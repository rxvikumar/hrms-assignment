package net.guides.springboot2.crud.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * OvertimeEntry entity tracking overtime hours, rate, and settlement status per worker per day.
 *
 * Design decisions:
 * - BigDecimal for all monetary values and hours (no float/double)
 * - FetchType.LAZY on all @ManyToOne
 * - Indexed on worker_id + date for monthly summary queries
 * - Indexed on settlement_status for batch settlement operations
 * - Once SETTLED, entries are immutable (enforced in service layer)
 */
@Entity
@Table(name = "overtime_entries", indexes = {
    @Index(name = "idx_overtime_worker_date", columnList = "worker_id, date"),
    @Index(name = "idx_overtime_status", columnList = "settlement_status"),
    @Index(name = "idx_overtime_worker_status", columnList = "worker_id, settlement_status")
})
public class OvertimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceLog attendance;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "overtime_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    /**
     * The overtime rate applied to this entry.
     * Blended rate calculation: 1.5x for first 2 OT hours, 2x beyond that.
     */
    @Column(name = "overtime_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeRate;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 10)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    public OvertimeEntry() {
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

    public AttendanceLog getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceLog attendance) {
        this.attendance = attendance;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public BigDecimal getOvertimeRate() {
        return overtimeRate;
    }

    public void setOvertimeRate(BigDecimal overtimeRate) {
        this.overtimeRate = overtimeRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public SettlementStatus getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(SettlementStatus settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    @Override
    public String toString() {
        return "OvertimeEntry{id=" + id + ", date=" + date + ", hours=" + overtimeHours
                + ", amount=" + amount + ", status=" + settlementStatus + "}";
    }
}
