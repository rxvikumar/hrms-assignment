package net.guides.springboot2.crud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for individual overtime entry in a monthly summary.
 */
public class OvertimeDayDetail {

    private LocalDate date;
    private BigDecimal overtimeHours;
    private BigDecimal rate;
    private BigDecimal amount;
    private String settlementStatus;

    public OvertimeDayDetail() {
    }

    public OvertimeDayDetail(LocalDate date, BigDecimal overtimeHours, BigDecimal rate,
                             BigDecimal amount, String settlementStatus) {
        this.date = date;
        this.overtimeHours = overtimeHours;
        this.rate = rate;
        this.amount = amount;
        this.settlementStatus = settlementStatus;
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

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }
}
