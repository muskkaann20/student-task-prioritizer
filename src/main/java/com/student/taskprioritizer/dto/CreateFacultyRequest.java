package com.student.taskprioritizer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

/** Request body for creating a faculty account (admin only). */
public class CreateFacultyRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Designation must not be blank")
    private String designation;

    @Min(value = 1, message = "Max hours per week must be at least 1")
    @Max(value = 80, message = "Max hours per week must not exceed 80")
    private int maxHoursPerWeek;

    @Valid
    private List<SkillProficiencyDto> skills = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getMaxHoursPerWeek() { return maxHoursPerWeek; }
    public void setMaxHoursPerWeek(int maxHoursPerWeek) { this.maxHoursPerWeek = maxHoursPerWeek; }

    public List<SkillProficiencyDto> getSkills() { return skills; }
    public void setSkills(List<SkillProficiencyDto> skills) { this.skills = skills; }
}
