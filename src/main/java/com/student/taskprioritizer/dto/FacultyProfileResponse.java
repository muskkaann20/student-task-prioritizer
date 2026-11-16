package com.student.taskprioritizer.dto;

import java.util.List;
import java.util.UUID;

/** Response DTO for a faculty profile with skills. */
public class FacultyProfileResponse {

    private UUID userId;
    private String name;
    private String email;
    private String department;
    private String designation;
    private int maxHoursPerWeek;
    private double currentLoadHours;
    private List<SkillProficiencyDto> skills;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getMaxHoursPerWeek() { return maxHoursPerWeek; }
    public void setMaxHoursPerWeek(int maxHoursPerWeek) { this.maxHoursPerWeek = maxHoursPerWeek; }

    public double getCurrentLoadHours() { return currentLoadHours; }
    public void setCurrentLoadHours(double currentLoadHours) { this.currentLoadHours = currentLoadHours; }

    public List<SkillProficiencyDto> getSkills() { return skills; }
    public void setSkills(List<SkillProficiencyDto> skills) { this.skills = skills; }
}
