package net.guides.springboot2.crud.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.guides.springboot2.crud.model.AttendanceLog;

/**
 * Repository for AttendanceLog with JOIN FETCH queries to eliminate N+1 (Ticket LF-203).
 *
 * Key design decision: All queries that return AttendanceLog use JOIN FETCH
 * for Worker and Site to load them in a single SQL query instead of N+1.
 */
@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    /**
     * Find active clock-in for a worker (no clock-out yet).
     * Used to prevent duplicate clock-in and to find the record for clock-out.
     */
    @Query("SELECT a FROM AttendanceLog a " +
           "JOIN FETCH a.worker w " +
           "JOIN FETCH a.site s " +
           "WHERE w.id = :workerId AND a.clockOutTime IS NULL")
    Optional<AttendanceLog> findActiveClockIn(@Param("workerId") Long workerId);

    /**
     * Paginated attendance log for a worker within a date range (Ticket LF-203).
     * Uses JOIN FETCH to eliminate N+1 queries.
     * 
     * Note: countQuery is required when using JOIN FETCH with Pageable,
     * otherwise Hibernate fails to generate the count query.
     */
    @Query(value = "SELECT a FROM AttendanceLog a " +
                   "JOIN FETCH a.worker w " +
                   "JOIN FETCH a.site s " +
                   "WHERE w.id = :workerId " +
                   "AND a.clockInTime >= :fromDate " +
                   "AND a.clockInTime <= :toDate " +
                   "ORDER BY a.clockInTime DESC",
           countQuery = "SELECT COUNT(a) FROM AttendanceLog a " +
                        "WHERE a.worker.id = :workerId " +
                        "AND a.clockInTime >= :fromDate " +
                        "AND a.clockInTime <= :toDate")
    Page<AttendanceLog> findByWorkerAndDateRange(
            @Param("workerId") Long workerId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    /**
     * Paginated attendance log - all records (Ticket LF-203).
     * Uses JOIN FETCH to eliminate N+1 queries.
     */
    @Query(value = "SELECT a FROM AttendanceLog a " +
                   "JOIN FETCH a.worker w " +
                   "JOIN FETCH a.site s " +
                   "ORDER BY a.clockInTime DESC",
           countQuery = "SELECT COUNT(a) FROM AttendanceLog a")
    Page<AttendanceLog> findAllWithDetails(Pageable pageable);
}
