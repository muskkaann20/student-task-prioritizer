package com.student.taskprioritizer.model;

import jakarta.persistence.*;

/** Extended profile for faculty members. One-to-one with {@link User}. */
@Entity
@Table(name = "faculty_profiles")
public class FacultyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String designation;

    @Column(nullable = false)
    private Integer maxHoursPerWeek;

    public FacultyProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getMaxHoursPerWeek() { return maxHoursPerWeek; }
    public void setMaxHoursPerWeek(Integer maxHoursPerWeek) { this.maxHoursPerWeek = maxHoursPerWeek; }
}
