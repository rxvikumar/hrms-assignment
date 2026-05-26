package net.guides.springboot2.crud.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.guides.springboot2.crud.exception.BusinessException;
import net.guides.springboot2.crud.model.Worker;
import net.guides.springboot2.crud.repository.WorkerRepository;
import net.guides.springboot2.crud.service.ActiveWorkerCacheService;

/**
 * REST controller for Worker CRUD operations.
 * Needed so we can create/manage workers for the attendance system.
 */
@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerRepository workerRepository;
    private final ActiveWorkerCacheService cacheService;

    public WorkerController(WorkerRepository workerRepository,
                            ActiveWorkerCacheService cacheService) {
        this.workerRepository = workerRepository;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ResponseEntity<List<Worker>> getAllWorkers() {
        return ResponseEntity.ok(workerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> getWorkerById(@PathVariable Long id) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("WORKER_NOT_FOUND",
                        "Worker not found with id: " + id));
        return ResponseEntity.ok(worker);
    }

    @PostMapping
    public ResponseEntity<Worker> createWorker(@Valid @RequestBody Worker worker) {
        if (workerRepository.existsByPhone(worker.getPhone())) {
            throw new BusinessException("DUPLICATE_PHONE",
                    "A worker with phone " + worker.getPhone() + " already exists");
        }
        Worker saved = workerRepository.save(worker);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Worker> updateWorker(@PathVariable Long id,
                                               @Valid @RequestBody Worker workerDetails) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("WORKER_NOT_FOUND",
                        "Worker not found with id: " + id));

        worker.setName(workerDetails.getName());
        worker.setPhone(workerDetails.getPhone());
        worker.setDesignation(workerDetails.getDesignation());
        worker.setDailyWageRate(workerDetails.getDailyWageRate());
        worker.setActive(workerDetails.isActive());

        Worker updated = workerRepository.save(worker);

        // Invalidate Redis cache if worker profile changed (cache invalidation requirement)
        cacheService.invalidateWorkerCache(id);

        return ResponseEntity.ok(updated);
    }
}
