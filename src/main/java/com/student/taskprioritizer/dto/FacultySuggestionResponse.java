package com.student.taskprioritizer.dto;

import java.util.List;
import java.util.UUID;

/** Response DTO for faculty suggestion results. */
public class FacultySuggestionResponse {

    private UUID facultyUserId;
    private String facultyName;
    private String facultyEmail;
    private int proficiency;
    private double freeHours;
    private double currentLoadHours;
    private double score;
    private List<String> reasons;

    public UUID getFacultyUserId() { return facultyUserId; }
    public void setFacultyUserId(UUID facultyUserId) { this.facultyUserId = facultyUserId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getFacultyEmail() { return facultyEmail; }
    public void setFacultyEmail(String facultyEmail) { this.facultyEmail = facultyEmail; }

    public int getProficiency() { return proficiency; }
    public void setProficiency(int proficiency) { this.proficiency = proficiency; }

    public double getFreeHours() { return freeHours; }
    public void setFreeHours(double freeHours) { this.freeHours = freeHours; }

    public double getCurrentLoadHours() { return currentLoadHours; }
    public void setCurrentLoadHours(double currentLoadHours) { this.currentLoadHours = currentLoadHours; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}
