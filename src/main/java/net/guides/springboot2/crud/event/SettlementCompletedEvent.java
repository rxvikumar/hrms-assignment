package net.guides.springboot2.crud.event;

import java.math.BigDecimal;

/**
 * Domain event published AFTER overtime settlement transaction commits.
 * Carries all data needed for the SMS notification.
 *
 * Why an event? (Ticket LF-204):
 * The SMS must fire AFTER the transaction commits, not during.
 * If the DB rolls back, no SMS should ever be sent.
 * If the SMS fails after successful commit, the data stays correct.
 */
public class SettlementCompletedEvent {

    private final Long workerId;
    private final String workerName;
    private final String workerPhone;
    private final String month;
    private final BigDecimal totalAmount;

    public SettlementCompletedEvent(Long workerId, String workerName, String workerPhone,
                                    String month, BigDecimal totalAmount) {
        this.workerId = workerId;
        this.workerName = workerName;
        this.workerPhone = workerPhone;
        this.month = month;
        this.totalAmount = totalAmount;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public String getWorkerPhone() {
        return workerPhone;
    }

    public String getMonth() {
        return month;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
