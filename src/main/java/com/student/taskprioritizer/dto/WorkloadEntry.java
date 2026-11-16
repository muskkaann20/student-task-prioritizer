package com.student.taskprioritizer.dto;

import java.util.UUID;

/** Row in the admin workload view. */
public class WorkloadEntry {

    private UUID facultyUserId;
    private String facultyName;
    private String weekLabel;
    private double assignedHours;
    private int maxHoursPerWeek;

    public UUID getFacultyUserId() { return facultyUserId; }
    public void setFacultyUserId(UUID facultyUserId) { this.facultyUserId = facultyUserId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getWeekLabel() { return weekLabel; }
    public void setWeekLabel(String weekLabel) { this.weekLabel = weekLabel; }

    public double getAssignedHours() { return assignedHours; }
    public void setAssignedHours(double assignedHours) { this.assignedHours = assignedHours; }

    public int getMaxHoursPerWeek() { return maxHoursPerWeek; }
    public void setMaxHoursPerWeek(int maxHoursPerWeek) { this.maxHoursPerWeek = maxHoursPerWeek; }
}
