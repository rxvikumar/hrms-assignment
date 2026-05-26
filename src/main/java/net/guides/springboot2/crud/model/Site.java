package net.guides.springboot2.crud.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * Site entity representing a construction site location.
 *
 * Design decisions:
 * - Indexed on active status for quick filtering of active sites
 * - site_name is unique at DB level to prevent duplicates
 */
@Entity
@Table(name = "sites", indexes = {
    @Index(name = "idx_site_active", columnList = "active"),
    @Index(name = "idx_site_name", columnList = "site_name", unique = true)
})
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Site name is required")
    @Column(name = "site_name", nullable = false, unique = true, length = 150)
    private String siteName;

    @NotBlank(message = "Location is required")
    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Site() {
    }

    public Site(String siteName, String location) {
        this.siteName = siteName;
        this.location = location;
        this.active = true;
    }

    @javax.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Site{id=" + id + ", siteName='" + siteName + "', location='" + location + "'}";
    }
}
