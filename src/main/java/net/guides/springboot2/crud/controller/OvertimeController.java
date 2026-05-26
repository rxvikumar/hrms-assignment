package net.guides.springboot2.crud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.guides.springboot2.crud.dto.OvertimeSummaryResponse;
import net.guides.springboot2.crud.dto.SettlementResponse;
import net.guides.springboot2.crud.service.OvertimeService;

/**
 * REST controller for overtime operations.
 * All endpoints under /api/overtime as specified in the assignment.
 */
@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {

    private final OvertimeService overtimeService;

    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    /**
     * GET /api/overtime/summary/{workerId}?month={YYYY-MM}
     * Monthly overtime summary with total hours, breakdown by date,
     * total payout amount, and settlement status.
     */
    @GetMapping("/summary/{workerId}")
    public ResponseEntity<OvertimeSummaryResponse> getMonthlySummary(
            @PathVariable Long workerId,
            @RequestParam String month) {

        OvertimeSummaryResponse response = overtimeService.getMonthlySummary(workerId, month);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/overtime/settle/{workerId}?month={YYYY-MM}
     * Mark all overtime entries for a worker+month as SETTLED.
     * Cannot settle the current month (only past months).
     * Returns total amount in the response.
     */
    @PostMapping("/settle/{workerId}")
    public ResponseEntity<SettlementResponse> settleOvertime(
            @PathVariable Long workerId,
            @RequestParam String month) {

        SettlementResponse response = overtimeService.settleOvertime(workerId, month);
        return ResponseEntity.ok(response);
    }
}
