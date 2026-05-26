package net.guides.springboot2.crud.service;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import net.guides.springboot2.crud.dto.ActiveWorkerInfo;

/**
 * Redis-backed cache for active (clocked-in) workers.
 * 
 * Ticket LF-202: All operations degrade gracefully when Redis is down.
 * Every Redis operation is wrapped in try-catch. If Redis fails, the operation
 * is logged and skipped — the system continues using the database.
 *
 * Design:
 * - Uses a Redis Hash with key "active_workers" and field = workerId
 * - TTL of 16 hours per entry (safety net for missed clock-outs)
 * - When Redis is unavailable, methods return empty/null gracefully
 */
@Service
public class ActiveWorkerCacheService {

    private static final Logger log = LoggerFactory.getLogger(ActiveWorkerCacheService.class);

    private static final String ACTIVE_WORKERS_KEY = "active_workers";
    private static final long TTL_HOURS = 16; // Safety net: auto-expire after 16 hours

    private final RedisTemplate<String, Object> redisTemplate;

    public ActiveWorkerCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add a worker to the active workers cache on clock-in.
     * Degrades gracefully if Redis is unavailable.
     */
    public void addActiveWorker(ActiveWorkerInfo info) {
        try {
            HashOperations<String, String, ActiveWorkerInfo> ops = redisTemplate.opsForHash();
            ops.put(ACTIVE_WORKERS_KEY, info.getWorkerId().toString(), info);
            // Set TTL on the hash key (refreshed on each write)
            redisTemplate.expire(ACTIVE_WORKERS_KEY, TTL_HOURS, TimeUnit.HOURS);
            log.debug("Added worker {} to active cache", info.getWorkerId());
        } catch (Exception ex) {
            log.warn("Redis unavailable: could not cache active worker {}. " +
                     "System continues with DB-only mode. Error: {}",
                     info.getWorkerId(), ex.getMessage());
        }
    }

    /**
     * Remove a worker from the active workers cache on clock-out.
     * Degrades gracefully if Redis is unavailable.
     */
    public void removeActiveWorker(Long workerId) {
        try {
            HashOperations<String, String, ActiveWorkerInfo> ops = redisTemplate.opsForHash();
            ops.delete(ACTIVE_WORKERS_KEY, workerId.toString());
            log.debug("Removed worker {} from active cache", workerId);
        } catch (Exception ex) {
            log.warn("Redis unavailable: could not remove worker {} from cache. Error: {}",
                     workerId, ex.getMessage());
        }
    }

    /**
     * Get all currently active workers from Redis.
     * Returns empty collection if Redis is unavailable (graceful degradation).
     */
    public Collection<ActiveWorkerInfo> getAllActiveWorkers() {
        try {
            HashOperations<String, String, ActiveWorkerInfo> ops = redisTemplate.opsForHash();
            return ops.values(ACTIVE_WORKERS_KEY);
        } catch (Exception ex) {
            log.warn("Redis unavailable: cannot fetch active workers from cache. " +
                     "Returning empty list. Error: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Invalidate a specific worker's cached entry when their profile is updated.
     * Called when worker name, designation, or wage rate changes.
     */
    public void invalidateWorkerCache(Long workerId) {
        try {
            HashOperations<String, String, ActiveWorkerInfo> ops = redisTemplate.opsForHash();
            if (ops.hasKey(ACTIVE_WORKERS_KEY, workerId.toString())) {
                ops.delete(ACTIVE_WORKERS_KEY, workerId.toString());
                log.info("Invalidated cache for worker {} due to profile update", workerId);
            }
        } catch (Exception ex) {
            log.warn("Redis unavailable: could not invalidate cache for worker {}. Error: {}",
                     workerId, ex.getMessage());
        }
    }
}
