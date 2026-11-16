package com.student.taskprioritizer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Skill + proficiency pair used when creating or updating a faculty member. */
public class SkillProficiencyDto {

    @NotBlank(message = "Skill name must not be blank")
    private String skillName;

    @Min(value = 1, message = "Proficiency must be at least 1")
    @Max(value = 5, message = "Proficiency must be at most 5")
    private int proficiency;

    public SkillProficiencyDto() {}

    public SkillProficiencyDto(String skillName, int proficiency) {
        this.skillName = skillName;
        this.proficiency = proficiency;
    }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public int getProficiency() { return proficiency; }
    public void setProficiency(int proficiency) { this.proficiency = proficiency; }
}
