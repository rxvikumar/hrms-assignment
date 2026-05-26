package net.guides.springboot2.crud.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.guides.springboot2.crud.dto.ActiveWorkerInfo;
import net.guides.springboot2.crud.dto.AttendanceResponse;
import net.guides.springboot2.crud.dto.ClockInRequest;
import net.guides.springboot2.crud.dto.ClockOutRequest;
import net.guides.springboot2.crud.dto.PaginatedResponse;
import net.guides.springboot2.crud.service.AttendanceService;

/**
 * REST controller for attendance operations.
 * All endpoints under /api/attendance as specified in the assignment.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * POST /api/attendance/clock-in
     * Log a worker's arrival at a site.
     */
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(@Valid @RequestBody ClockInRequest request) {
        AttendanceResponse response = attendanceService.clockIn(
                request.getWorkerId(), request.getSiteId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST /api/attendance/clock-out
     * Log a worker's departure. System calculates total hours and overtime automatically.
     */
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(@Valid @RequestBody ClockOutRequest request) {
        AttendanceResponse response = attendanceService.clockOut(request.getWorkerId());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/attendance/active
     * List all workers currently clocked in across all sites.
     * Served from Redis, not the database (assignment requirement).
     */
    @GetMapping("/active")
    public ResponseEntity<List<ActiveWorkerInfo>> getActiveWorkers() {
        List<ActiveWorkerInfo> activeWorkers = attendanceService.getActiveWorkers();
        return ResponseEntity.ok(activeWorkers);
    }

    /**
     * GET /api/attendance/log?workerId={id}&from={date}&to={date}&page={n}&size={n}
     * Attendance history for a worker in a date range. Supports pagination (Ticket LF-203).
     *
     * Defaults: page=0, size=20 (configured in application.properties)
     * The old unparameterized call still works but returns only the first page.
     */
    @GetMapping("/log")
    public ResponseEntity<PaginatedResponse<AttendanceResponse>> getAttendanceLog(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Safety net: cap page size (Ticket LF-203)
        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);
        PaginatedResponse<AttendanceResponse> response =
                attendanceService.getAttendanceLog(workerId, from, to, pageable);
        return ResponseEntity.ok(response);
    }
}
