package net.guides.springboot2.crud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * AttendanceLog entity tracking worker clock-in/clock-out at construction sites.
 *
 * Design decisions:
 * - FetchType.LAZY on all @ManyToOne to prevent N+1 (Ticket LF-203)
 * - totalHoursWorked and overtimeHours stored as BigDecimal for precision
 * - flagged boolean for shifts exceeding 16 hours (business rule)
 * - Composite index on (worker_id, clock_in_time) for date range queries
 */
@Entity
@Table(name = "attendance_logs", indexes = {
    @Index(name = "idx_attendance_worker_clockin", columnList = "worker_id, clock_in_time"),
    @Index(name = "idx_attendance_worker_id", columnList = "worker_id"),
    @Index(name = "idx_attendance_site_id", columnList = "site_id"),
    @Index(name = "idx_attendance_clockin", columnList = "clock_in_time")
})
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "total_hours_worked", precision = 5, scale = 2)
    private BigDecimal totalHoursWorked;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    /**
     * Flagged for review if total shift exceeds 16 hours.
     * Business rule: nobody should be clocked in > 16 hours.
     */
    @Column(name = "flagged", nullable = false)
    private boolean flagged = false;

    public AttendanceLog() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public BigDecimal getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public void setTotalHoursWorked(BigDecimal totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    @Override
    public String toString() {
        return "AttendanceLog{id=" + id + ", clockIn=" + clockInTime + ", clockOut=" + clockOutTime + "}";
    }
}
