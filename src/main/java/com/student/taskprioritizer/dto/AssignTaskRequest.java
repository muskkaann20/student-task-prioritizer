package com.student.taskprioritizer.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Request body for assigning a task to a faculty member (admin only). */
public class AssignTaskRequest {

    @NotNull(message = "Faculty user ID is required")
    private UUID facultyUserId;

    public AssignTaskRequest() {}

    public UUID getFacultyUserId() { return facultyUserId; }
    public void setFacultyUserId(UUID facultyUserId) { this.facultyUserId = facultyUserId; }
}
