package net.guides.springboot2.crud.dto;

import java.math.BigDecimal;

/**
 * Response DTO for settlement operation.
 * Returns the total amount settled.
 */
public class SettlementResponse {

    private Long workerId;
    private String workerName;
    private String month;
    private int entriesSettled;
    private BigDecimal totalAmountSettled;
    private String message;

    public SettlementResponse() {
    }

    public SettlementResponse(Long workerId, String workerName, String month,
                              int entriesSettled, BigDecimal totalAmountSettled) {
        this.workerId = workerId;
        this.workerName = workerName;
        this.month = month;
        this.entriesSettled = entriesSettled;
        this.totalAmountSettled = totalAmountSettled;
        this.message = "Settlement completed for " + workerName + " for " + month;
    }

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

    public int getEntriesSettled() {
        return entriesSettled;
    }

    public void setEntriesSettled(int entriesSettled) {
        this.entriesSettled = entriesSettled;
    }

    public BigDecimal getTotalAmountSettled() {
        return totalAmountSettled;
    }

    public void setTotalAmountSettled(BigDecimal totalAmountSettled) {
        this.totalAmountSettled = totalAmountSettled;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
