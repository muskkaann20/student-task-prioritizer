package com.student.taskprioritizer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a task in the system — a student's own deadline or an admin-created
 * task assigned to a faculty member.
 *
 * <p>Migration note: the original {@code owner_id} VARCHAR column (legacy UUID
 * string from localStorage) is kept untouched. The new {@code user_id} FK column
 * holds the owning {@link User} for all tasks going forward.
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String title;

    /** Task category — stored as the enum name string so existing DB rows map directly. */
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Column(name = "due_date", nullable = false)
    @Convert(converter = LocalDateEpochConverter.class)
    private LocalDate dueDate;

    private String description;

    /**
     * Legacy string owner identifier written by the old single-user SPA
     * (a random UUID stored in localStorage). Kept for historical data;
     * new code uses {@link #owner} instead.
     */
    @Column(name = "owner_id")
    private String ownerId;

    @Column(nullable = false)
    private boolean completed = false;

    // ── Phase-1 additions ────────────────────────────────────────────

    /**
     * Estimated effort in hours. Required when an admin creates a task so
     * the suggestion engine can compute free-hours constraints.
     */
    private Double estimatedHours;

    /**
     * Task lifecycle status. Nullable in the DB to tolerate migrated rows;
     * the DataLoader back-fills PENDING for any null row at startup.
     */
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    /** Skill the assigned faculty member must possess (nullable — not every task has a skill requirement). */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "required_skill_id")
    private Skill requiredSkill;

    /**
     * The owning user (student for SPA-created tasks; null for admin-created
     * faculty tasks). This is the new FK column ({@code user_id}) introduced in
     * Phase 1 — separate from the legacy {@code owner_id} string column.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;

    /** Faculty member to whom the task is assigned. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    /** Admin who created/assigned the task. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by_id")
    private User assignedBy;

    /** Reason given by faculty when declining an assignment. */
    @Column(name = "decline_reason")
    private String declineReason;

    // ── Constructors ─────────────────────────────────────────────────

    public Task() {}

    public Task(String subject, String title, TaskType taskType,
                LocalDate dueDate, String description) {
        this.subject = subject;
        this.title = title;
        this.taskType = taskType;
        this.dueDate = dueDate;
        this.description = description;
    }

    // ── Getters & Setters ────────────────────────────────────────────

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

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Skill getRequiredSkill() { return requiredSkill; }
    public void setRequiredSkill(Skill requiredSkill) { this.requiredSkill = requiredSkill; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }

    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
}
