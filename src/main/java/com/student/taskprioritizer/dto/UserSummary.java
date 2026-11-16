package com.student.taskprioritizer.dto;

import java.util.UUID;

/** Compact user representation returned in nested response objects. */
public class UserSummary {

    private UUID id;
    private String name;
    private String email;
    private String role;

    public UserSummary() {}

    public UserSummary(UUID id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
