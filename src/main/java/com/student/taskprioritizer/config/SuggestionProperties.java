package com.student.taskprioritizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Faculty-suggestion scoring weights. Bound from {@code suggestion.weight.*}
 * in application.properties so they can be tuned without editing code.
 */
@ConfigurationProperties(prefix = "suggestion.weight")
public class SuggestionProperties {

    private double proficiency = 3;
    private double freeHours = 1;
    private double currentLoad = 2;

    public double getProficiency() { return proficiency; }
    public void setProficiency(double proficiency) { this.proficiency = proficiency; }

    public double getFreeHours() { return freeHours; }
    public void setFreeHours(double freeHours) { this.freeHours = freeHours; }

    public double getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(double currentLoad) { this.currentLoad = currentLoad; }
}
