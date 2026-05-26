package net.guides.springboot2.crud.dto;

import javax.validation.constraints.NotNull;

/**
 * Request DTO for clock-out operation.
 */
public class ClockOutRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    public ClockOutRequest() {
    }

    public ClockOutRequest(Long workerId) {
        this.workerId = workerId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }
}
