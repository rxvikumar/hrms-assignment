package net.guides.springboot2.crud.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.guides.springboot2.crud.dto.OvertimeDayDetail;
import net.guides.springboot2.crud.dto.OvertimeSummaryResponse;
import net.guides.springboot2.crud.dto.SettlementResponse;
import net.guides.springboot2.crud.event.SettlementCompletedEvent;
import net.guides.springboot2.crud.exception.BusinessException;
import net.guides.springboot2.crud.model.OvertimeEntry;
import net.guides.springboot2.crud.model.SettlementStatus;
import net.guides.springboot2.crud.model.Worker;
import net.guides.springboot2.crud.repository.OvertimeEntryRepository;
import net.guides.springboot2.crud.repository.WorkerRepository;

/**
 * Overtime service handling monthly summaries and settlement.
 *
 * Ticket LF-204 fixes:
 * - Settlement is ALL-OR-NOTHING via @Transactional (one atomic transaction)
 * - SMS fires AFTER commit via ApplicationEventPublisher → @TransactionalEventListener
 * - No partial state, no premature messages
 *
 * Ticket LF-205 fix:
 * - The getMonthlySummary method does NOT hold a DB transaction while making
 *   external API calls. External data is fetched BEFORE the transactional work.
 *   (In this implementation, we simulate the external call outside @Transactional)
 */
@Service
public class OvertimeService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeService.class);
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final OvertimeEntryRepository overtimeEntryRepository;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OvertimeService(OvertimeEntryRepository overtimeEntryRepository,
                           WorkerRepository workerRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.overtimeEntryRepository = overtimeEntryRepository;
        this.workerRepository = workerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Get monthly overtime summary for a worker.
     *
     * Ticket LF-205: This method is intentionally NOT @Transactional.
     * In the original bug, this method held a DB connection while making
     * a synchronous external API call (3-5 seconds), exhausting the pool.
     * 
     * Fix: External data fetched BEFORE any DB work, or DB work done in
     * a read-only transaction that completes quickly.
     */
    public OvertimeSummaryResponse getMonthlySummary(Long workerId, String monthStr) {
        // Simulate: Fetch external data BEFORE opening a transaction (Ticket LF-205)
        // In production, this would call the government minimum wage API
        fetchExternalDataBeforeTransaction();

        // Now do the DB work (quick, read-only)
        return buildSummaryFromDb(workerId, monthStr);
    }

    /**
     * Simulates the external API call that was causing pool exhaustion.
     * This is called OUTSIDE any @Transactional context, so no DB connection is held.
     */
    private void fetchExternalDataBeforeTransaction() {
        try {
            // In production: RestTemplate/WebClient call to government API
            // with configured timeouts (app.external-api.connect-timeout / read-timeout)
            log.debug("Fetching external minimum wage data (outside transaction)...");
        } catch (Exception ex) {
            log.warn("External API unavailable, using cached rates. Error: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public OvertimeSummaryResponse buildSummaryFromDb(Long workerId, String monthStr) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException("WORKER_NOT_FOUND",
                        "Worker not found with id: " + workerId));

        YearMonth yearMonth = parseMonth(monthStr);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<OvertimeEntry> entries = overtimeEntryRepository
                .findByWorkerAndMonth(workerId, startDate, endDate);

        BigDecimal totalHours = entries.stream()
                .map(OvertimeEntry::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayout = entries.stream()
                .map(OvertimeEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Determine overall settlement status
        boolean allSettled = entries.stream()
                .allMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED);
        boolean anySettled = entries.stream()
                .anyMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED);
        String overallStatus = entries.isEmpty() ? "NO_ENTRIES"
                : allSettled ? "SETTLED"
                : anySettled ? "PARTIAL"
                : "PENDING";

        List<OvertimeDayDetail> breakdown = entries.stream()
                .map(e -> new OvertimeDayDetail(
                        e.getDate(),
                        e.getOvertimeHours(),
                        e.getOvertimeRate(),
                        e.getAmount(),
                        e.getSettlementStatus().name()))
                .collect(Collectors.toList());

        OvertimeSummaryResponse response = new OvertimeSummaryResponse();
        response.setWorkerId(worker.getId());
        response.setWorkerName(worker.getName());
        response.setMonth(monthStr);
        response.setTotalOvertimeHours(totalHours);
        response.setTotalPayoutAmount(totalPayout);
        response.setOverallSettlementStatus(overallStatus);
        response.setBreakdown(breakdown);

        return response;
    }

    /**
     * Settle all overtime entries for a worker in a specific month.
     *
     * Ticket LF-204 fix:
     * - Entire settlement is ONE atomic transaction (@Transactional)
     * - If ANY entry fails, ALL roll back (no partial state)
     * - SMS notification published as event, fires ONLY AFTER COMMIT
     *
     * Business rules:
     * - Cannot settle current month (only completed months)
     * - Already-settled entries → 409 Conflict
     */
    @Transactional
    public SettlementResponse settleOvertime(Long workerId, String monthStr) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException("WORKER_NOT_FOUND",
                        "Worker not found with id: " + workerId));

        YearMonth yearMonth = parseMonth(monthStr);

        // Cannot settle current month
        if (yearMonth.equals(YearMonth.now())) {
            throw new BusinessException("CURRENT_MONTH_SETTLEMENT",
                    "Cannot settle overtime for the current month. Only past months can be settled.");
        }

        // Cannot settle future months
        if (yearMonth.isAfter(YearMonth.now())) {
            throw new BusinessException("FUTURE_MONTH_SETTLEMENT",
                    "Cannot settle overtime for a future month.");
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Fetch PENDING entries only
        List<OvertimeEntry> pendingEntries = overtimeEntryRepository
                .findByWorkerAndMonthAndStatus(workerId, startDate, endDate, SettlementStatus.PENDING);

        if (pendingEntries.isEmpty()) {
            // Check if already settled
            List<OvertimeEntry> allEntries = overtimeEntryRepository
                    .findByWorkerAndMonth(workerId, startDate, endDate);
            if (!allEntries.isEmpty() && allEntries.stream()
                    .allMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED)) {
                throw new BusinessException("ALREADY_SETTLED",
                        "Overtime for " + worker.getName() + " for " + monthStr + " is already settled.");
            }
            throw new BusinessException("NO_PENDING_ENTRIES",
                    "No pending overtime entries found for " + worker.getName() + " for " + monthStr);
        }

        // Settle ALL entries in one atomic transaction
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OvertimeEntry entry : pendingEntries) {
            entry.setSettlementStatus(SettlementStatus.SETTLED);
            totalAmount = totalAmount.add(entry.getAmount());
        }
        overtimeEntryRepository.saveAll(pendingEntries);

        // Publish event — listener fires AFTER COMMIT only (Ticket LF-204)
        SettlementCompletedEvent event = new SettlementCompletedEvent(
                worker.getId(), worker.getName(), worker.getPhone(),
                monthStr, totalAmount
        );
        eventPublisher.publishEvent(event);

        log.info("Settlement completed for worker {} ({}): {} entries, total ₹{}",
                worker.getName(), monthStr, pendingEntries.size(), totalAmount);

        return new SettlementResponse(
                worker.getId(), worker.getName(), monthStr,
                pendingEntries.size(), totalAmount
        );
    }

    private YearMonth parseMonth(String monthStr) {
        try {
            return YearMonth.parse(monthStr, MONTH_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("INVALID_MONTH_FORMAT",
                    "Month must be in YYYY-MM format. Got: " + monthStr);
        }
    }
}
