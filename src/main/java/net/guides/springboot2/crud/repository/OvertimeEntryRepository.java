package net.guides.springboot2.crud.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.guides.springboot2.crud.model.OvertimeEntry;
import net.guides.springboot2.crud.model.SettlementStatus;

/**
 * Repository for OvertimeEntry with JOIN FETCH and aggregation queries.
 */
@Repository
public interface OvertimeEntryRepository extends JpaRepository<OvertimeEntry, Long> {

    /**
     * Fetch all overtime entries for a worker in a given month with JOIN FETCH.
     */
    @Query("SELECT o FROM OvertimeEntry o " +
           "JOIN FETCH o.worker w " +
           "JOIN FETCH o.attendance a " +
           "WHERE w.id = :workerId " +
           "AND o.date >= :startDate " +
           "AND o.date <= :endDate " +
           "ORDER BY o.date ASC")
    List<OvertimeEntry> findByWorkerAndMonth(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Fetch all PENDING overtime entries for settlement (worker + month).
     */
    @Query("SELECT o FROM OvertimeEntry o " +
           "JOIN FETCH o.worker w " +
           "JOIN FETCH o.attendance a " +
           "WHERE w.id = :workerId " +
           "AND o.date >= :startDate " +
           "AND o.date <= :endDate " +
           "AND o.settlementStatus = :status " +
           "ORDER BY o.date ASC")
    List<OvertimeEntry> findByWorkerAndMonthAndStatus(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") SettlementStatus status);

    /**
     * Sum total overtime hours for a worker in a given month.
     * Used to enforce the 60-hour monthly cap.
     */
    @Query("SELECT COALESCE(SUM(o.overtimeHours), 0) FROM OvertimeEntry o " +
           "WHERE o.worker.id = :workerId " +
           "AND o.date >= :startDate " +
           "AND o.date <= :endDate")
    BigDecimal sumOvertimeHoursForWorkerInMonth(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
