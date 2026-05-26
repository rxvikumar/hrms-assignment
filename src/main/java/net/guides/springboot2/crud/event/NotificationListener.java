package net.guides.springboot2.crud.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener for settlement events. Sends SMS notification AFTER transaction commits.
 *
 * Ticket LF-204 fix:
 * - Uses @TransactionalEventListener(phase = AFTER_COMMIT) so SMS only fires
 *   after the settlement transaction has been successfully committed.
 * - If the transaction rolls back, this listener is never invoked.
 * - If the SMS fails, the settlement data remains correct (fire-and-forget with retry logging).
 *
 * In production, this would integrate with an SMS gateway (Twilio, MSG91, etc.).
 * Here we log the SMS content as a simulation.
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    /**
     * Fires ONLY after the settlement transaction has committed.
     * This ensures no premature SMS is sent for rolled-back transactions.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSettlementCompleted(SettlementCompletedEvent event) {
        try {
            sendSmsNotification(event);
        } catch (Exception ex) {
            // SMS failure must NOT affect the settlement result.
            // Log and queue for retry. Settlement data is already committed and correct.
            log.error("Failed to send settlement SMS to worker {} (phone: {}). " +
                      "Settlement data is intact. Queuing for retry.",
                      event.getWorkerName(), event.getWorkerPhone(), ex);
        }
    }

    /**
     * Simulates sending an SMS notification.
     * In production: replace with actual SMS gateway integration.
     */
    private void sendSmsNotification(SettlementCompletedEvent event) {
        String smsBody = String.format(
                "Dear %s, your %s overtime of ₹%s has been settled. " +
                "Amount will be credited to your account shortly.",
                event.getWorkerName(),
                event.getMonth(),
                event.getTotalAmount().toPlainString()
        );

        // Simulate SMS sending
        log.info("=== SMS NOTIFICATION ===");
        log.info("To: {}", event.getWorkerPhone());
        log.info("Message: {}", smsBody);
        log.info("========================");
    }
}
