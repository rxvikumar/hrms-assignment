package net.guides.springboot2.crud.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for monthly overtime summary.
 * Contains total hours, breakdown by date, total payout, and settlement status.
 */
public class OvertimeSummaryResponse {

    private Long workerId;
    private String workerName;
    private String month;
    private BigDecimal totalOvertimeHours;
    private BigDecimal totalPayoutAmount;
    private String overallSettlementStatus;
    private List<OvertimeDayDetail> breakdown;

    public OvertimeSummaryResponse() {
    }

    // --- Getters and Setters ---

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public BigDecimal getTotalPayoutAmount() {
        return totalPayoutAmount;
    }

    public void setTotalPayoutAmount(BigDecimal totalPayoutAmount) {
        this.totalPayoutAmount = totalPayoutAmount;
    }

    public String getOverallSettlementStatus() {
        return overallSettlementStatus;
    }

    public void setOverallSettlementStatus(String overallSettlementStatus) {
        this.overallSettlementStatus = overallSettlementStatus;
    }

    public List<OvertimeDayDetail> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(List<OvertimeDayDetail> breakdown) {
        this.breakdown = breakdown;
    }
}
