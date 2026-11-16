package com.student.taskprioritizer.model;

import jakarta.persistence.*;

/** Associates a {@link FacultyProfile} with a {@link Skill} and a proficiency level 1–5. */
@Entity
@Table(name = "faculty_skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"faculty_profile_id", "skill_id"}))
public class FacultySkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_profile_id")
    private FacultyProfile facultyProfile;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    /** Proficiency level: 1 (basic) to 5 (expert). */
    @Column(nullable = false)
    private int proficiency;

    public FacultySkill() {}

    public FacultySkill(FacultyProfile facultyProfile, Skill skill, int proficiency) {
        this.facultyProfile = facultyProfile;
        this.skill = skill;
        this.proficiency = proficiency;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FacultyProfile getFacultyProfile() { return facultyProfile; }
    public void setFacultyProfile(FacultyProfile facultyProfile) { this.facultyProfile = facultyProfile; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public int getProficiency() { return proficiency; }
    public void setProficiency(int proficiency) { this.proficiency = proficiency; }
}
