package com.student.taskprioritizer.dto;

import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.model.TaskStatus;
import com.student.taskprioritizer.model.TaskType;

import java.time.LocalDate;
import java.util.UUID;

/** Response DTO for a task, with owner/assignee info flattened. */
public class TaskResponse {

    private Long id;
    private String subject;
    private String title;
    private TaskType taskType;
    private LocalDate dueDate;
    private String description;
    private Double estimatedHours;
    private TaskStatus status;
    private boolean completed;
    private String requiredSkillName;

    // Owner info
    private UUID ownerId;
    private String ownerName;

    // Assignment info
    private UUID assignedToId;
    private String assignedToName;
    private UUID assignedById;
    private String assignedByName;

    private String declineReason;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getRequiredSkillName() { return requiredSkillName; }
    public void setRequiredSkillName(String requiredSkillName) { this.requiredSkillName = requiredSkillName; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public UUID getAssignedToId() { return assignedToId; }
    public void setAssignedToId(UUID assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public UUID getAssignedById() { return assignedById; }
    public void setAssignedById(UUID assignedById) { this.assignedById = assignedById; }

    public String getAssignedByName() { return assignedByName; }
    public void setAssignedByName(String assignedByName) { this.assignedByName = assignedByName; }

    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    public static TaskResponse from(Task task) {
        if (task == null) return null;
        TaskResponse r = new TaskResponse();
        r.setId(task.getId());
        r.setSubject(task.getSubject());
        r.setTitle(task.getTitle());
        r.setTaskType(task.getTaskType());
        r.setDueDate(task.getDueDate());
        r.setDescription(task.getDescription());
        r.setCompleted(task.isCompleted());
        r.setEstimatedHours(task.getEstimatedHours());
        r.setStatus(task.getStatus());
        if (task.getRequiredSkill() != null) {
            r.setRequiredSkillName(task.getRequiredSkill().getName());
        }
        if (task.getOwner() != null) {
            r.setOwnerId(task.getOwner().getId());
            r.setOwnerName(task.getOwner().getName());
        }
        if (task.getAssignedTo() != null) {
            r.setAssignedToId(task.getAssignedTo().getId());
            r.setAssignedToName(task.getAssignedTo().getName());
        }
        if (task.getAssignedBy() != null) {
            r.setAssignedById(task.getAssignedBy().getId());
            r.setAssignedByName(task.getAssignedBy().getName());
        }
        r.setDeclineReason(task.getDeclineReason());
        return r;
    }
}
