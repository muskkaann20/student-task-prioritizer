package com.student.taskprioritizer.dto;

import com.student.taskprioritizer.model.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Request body for updating an existing task. */
public class UpdateTaskRequest {

    @NotBlank(message = "Subject must not be blank")
    private String subject;

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotNull(message = "Task type is required")
    private TaskType taskType;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String description;

    @Positive(message = "Estimated hours must be positive")
    private Double estimatedHours;

    private Long requiredSkillId;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Long getRequiredSkillId() { return requiredSkillId; }
    public void setRequiredSkillId(Long requiredSkillId) { this.requiredSkillId = requiredSkillId; }
}
