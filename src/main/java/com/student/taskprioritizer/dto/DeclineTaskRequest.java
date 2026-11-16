package com.student.taskprioritizer.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for declining a faculty task assignment. */
public class DeclineTaskRequest {

    @NotBlank(message = "A reason for declining is required")
    private String reason;

    public DeclineTaskRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
