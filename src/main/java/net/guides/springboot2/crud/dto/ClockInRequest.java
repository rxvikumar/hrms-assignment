package net.guides.springboot2.crud.dto;

import javax.validation.constraints.NotNull;

/**
 * Request DTO for clock-in operation.
 */
public class ClockInRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    public ClockInRequest() {
    }

    public ClockInRequest(Long workerId, Long siteId) {
        this.workerId = workerId;
        this.siteId = siteId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }
}
