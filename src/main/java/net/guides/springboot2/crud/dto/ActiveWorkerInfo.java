package net.guides.springboot2.crud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for active worker info stored in Redis and returned by GET /api/attendance/active.
 * This is what gets serialized to/from Redis.
 */
public class ActiveWorkerInfo {

    private Long workerId;
    private String workerName;
    private String designation;
    private Long siteId;
    private String siteName;
    private String siteLocation;
    private LocalDateTime clockInTime;
    private BigDecimal dailyWageRate;

    public ActiveWorkerInfo() {
    }

    public ActiveWorkerInfo(Long workerId, String workerName, String designation,
                            Long siteId, String siteName, String siteLocation,
                            LocalDateTime clockInTime, BigDecimal dailyWageRate) {
        this.workerId = workerId;
        this.workerName = workerName;
        this.designation = designation;
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteLocation = siteLocation;
        this.clockInTime = clockInTime;
        this.dailyWageRate = dailyWageRate;
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

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteLocation() {
        return siteLocation;
    }

    public void setSiteLocation(String siteLocation) {
        this.siteLocation = siteLocation;
    }

    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    public BigDecimal getDailyWageRate() {
        return dailyWageRate;
    }

    public void setDailyWageRate(BigDecimal dailyWageRate) {
        this.dailyWageRate = dailyWageRate;
    }
}
