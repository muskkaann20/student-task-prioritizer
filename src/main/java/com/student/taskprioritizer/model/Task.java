package com.student.taskprioritizer.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a student task — an assignment, exam, project, or placement test
 * with a subject and due date.
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

    /** One of: EXAM, ASSIGNMENT, PROJECT, PLACEMENT_TEST */
    @Column(name = "task_type", nullable = false)
    private String taskType;

    @Column(name = "due_date", nullable = false)
    @Convert(converter = LocalDateEpochConverter.class)
    private LocalDate dueDate;

    private String description;

    @Column(nullable = false)
    private boolean completed = false;

    // ---------- Constructors ----------

    public Task() {}

    public Task(String subject, String title, String taskType, LocalDate dueDate, String description) {
        this.subject = subject;
        this.title = title;
        this.taskType = taskType;
        this.dueDate = dueDate;
        this.description = description;
    }

    // ---------- Getters & Setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
