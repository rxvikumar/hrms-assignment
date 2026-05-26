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
import net.guides.springboot2.crud.model.Site;
import net.guides.springboot2.crud.repository.SiteRepository;

/**
 * REST controller for Site CRUD operations.
 * Needed so we can create/manage construction sites.
 */
@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteRepository siteRepository;

    public SiteController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @GetMapping
    public ResponseEntity<List<Site>> getAllSites() {
        return ResponseEntity.ok(siteRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Site> getSiteById(@PathVariable Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SITE_NOT_FOUND",
                        "Site not found with id: " + id));
        return ResponseEntity.ok(site);
    }

    @PostMapping
    public ResponseEntity<Site> createSite(@Valid @RequestBody Site site) {
        Site saved = siteRepository.save(site);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Site> updateSite(@PathVariable Long id,
                                           @Valid @RequestBody Site siteDetails) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SITE_NOT_FOUND",
                        "Site not found with id: " + id));

        site.setSiteName(siteDetails.getSiteName());
        site.setLocation(siteDetails.getLocation());
        site.setActive(siteDetails.isActive());

        return ResponseEntity.ok(siteRepository.save(site));
    }
}
