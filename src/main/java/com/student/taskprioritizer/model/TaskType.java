package com.student.taskprioritizer.model;

/**
 * Task category. Stored as a STRING in the database so existing rows
 * (EXAM, ASSIGNMENT, PROJECT, PLACEMENT_TEST) map without any migration.
 */
public enum TaskType {
    EXAM,
    ASSIGNMENT,
    PROJECT,
    PLACEMENT_TEST
}
