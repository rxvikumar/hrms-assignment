package net.guides.springboot2.crud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.DecimalMin;

/**
 * Worker entity representing a blue-collar construction worker.
 * 
 * Design decisions:
 * - dailyWageRate is BigDecimal (never float/double for money)
 * - phone has a unique constraint at DB level for data integrity
 * - Indexed on phone and designation for frequent lookups
 */
@Entity
@Table(name = "workers", indexes = {
    @Index(name = "idx_worker_phone", columnList = "phone", unique = true),
    @Index(name = "idx_worker_active", columnList = "active")
})
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Worker name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    @Column(name = "phone", nullable = false, unique = true, length = 10)
    private String phone;

    @NotNull(message = "Designation is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "designation", nullable = false, length = 20)
    private Designation designation;

    @NotNull(message = "Daily wage rate is required")
    @DecimalMin(value = "0.01", message = "Daily wage must be positive")
    @Column(name = "daily_wage_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWageRate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Worker() {
    }

    public Worker(String name, String phone, Designation designation, BigDecimal dailyWageRate) {
        this.name = name;
        this.phone = phone;
        this.designation = designation;
        this.dailyWageRate = dailyWageRate;
        this.active = true;
    }

    @javax.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @javax.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public BigDecimal getDailyWageRate() {
        return dailyWageRate;
    }

    public void setDailyWageRate(BigDecimal dailyWageRate) {
        this.dailyWageRate = dailyWageRate;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Worker{id=" + id + ", name='" + name + "', designation=" + designation + "}";
    }
}
