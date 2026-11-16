package com.student.taskprioritizer.service;

import com.student.taskprioritizer.config.SuggestionProperties;
import com.student.taskprioritizer.dto.FacultySuggestionResponse;
import com.student.taskprioritizer.model.FacultyProfile;
import com.student.taskprioritizer.model.FacultySkill;
import com.student.taskprioritizer.model.Skill;
import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.repository.FacultyProfileRepository;
import com.student.taskprioritizer.repository.FacultySkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rule-based faculty matching for admin task assignment.
 *
 * <p>Hard filters:
 * <ol>
 *   <li>Faculty must have the required skill (if set).</li>
 *   <li>Faculty must have enough free hours before the due date.</li>
 *   <li>Faculty must not be over their weekly maximum.</li>
 * </ol>
 *
 * <p>Score = {@code proficiency_weight * proficiency
 *            + free_hours_weight * (freeHours - estimatedHours)
 *            - current_load_weight * currentLoad}
 *
 * <p>Returns the top 3, sorted by score descending.
 */
@Service
public class SuggestionService {

    private final FacultyProfileRepository profileRepository;
    private final FacultySkillRepository facultySkillRepository;
    private final WorkloadService workloadService;
    private final SuggestionProperties weights;

    public SuggestionService(FacultyProfileRepository profileRepository,
                             FacultySkillRepository facultySkillRepository,
                             WorkloadService workloadService,
                             SuggestionProperties weights) {
        this.profileRepository = profileRepository;
        this.facultySkillRepository = facultySkillRepository;
        this.workloadService = workloadService;
        this.weights = weights;
    }

    @Transactional(readOnly = true)
    public List<FacultySuggestionResponse> suggest(Task task) {
        List<FacultySuggestionResponse> eligible = new ArrayList<>();

        for (FacultyProfile profile : profileRepository.findAll()) {
            FacultySuggestionResponse resp = score(profile, task);
            if (resp != null) {
                eligible.add(resp);
            }
        }

        eligible.sort(Comparator.comparingDouble(FacultySuggestionResponse::getScore).reversed());
        return eligible.stream().limit(3).toList();
    }

    /**
     * Scores a single faculty member for a task. Returns null if any hard
     * filter disqualifies them.
     */
    FacultySuggestionResponse score(FacultyProfile profile, Task task) {
        List<String> reasons = new ArrayList<>();
        Skill required = task.getRequiredSkill();
        int proficiency = 0;

        // Hard filter 1: skill match
        if (required != null) {
            FacultySkill match = facultySkillRepository
                    .findByFacultyProfileAndSkill(profile, required)
                    .orElse(null);
            if (match == null) {
                return null; // hard filter — no required skill
            }
            proficiency = match.getProficiency();
            reasons.add("Skill match: " + required.getName() + " (proficiency " + proficiency + "/5)");
        } else {
            reasons.add("No specific skill required");
        }

        // Compute load and capacity
        double estimatedHours = task.getEstimatedHours() == null ? 0 : task.getEstimatedHours();
        double currentLoad = workloadService.loadHoursForWeekOf(
                profile.getUser(), task.getDueDate());
        int maxHours = profile.getMaxHoursPerWeek() == null ? 0 : profile.getMaxHoursPerWeek();
        double freeHours = Math.max(0, maxHours - currentLoad);

        // Hard filter 2: enough free hours
        if (freeHours < estimatedHours) {
            return null; // not enough free hours
        }

        // Hard filter 3: not over weekly maximum
        if (currentLoad + estimatedHours > maxHours) {
            return null; // would exceed weekly max
        }

        reasons.add(round(freeHours) + " free hours (load " + round(currentLoad)
                + "h / max " + maxHours + "h)");

        // Score formula: 3*proficiency + 1*(freeHours - estimatedHours) - 2*currentLoad
        double scoreValue = weights.getProficiency() * proficiency
                + weights.getFreeHours() * (freeHours - estimatedHours)
                - weights.getCurrentLoad() * currentLoad;

        FacultySuggestionResponse resp = new FacultySuggestionResponse();
        resp.setFacultyUserId(profile.getUser().getId());
        resp.setFacultyName(profile.getUser().getName());
        resp.setFacultyEmail(profile.getUser().getEmail());
        resp.setProficiency(proficiency);
        resp.setCurrentLoadHours(round(currentLoad));
        resp.setFreeHours(round(freeHours));
        resp.setScore(Math.round(scoreValue * 100.0) / 100.0);
        resp.setReasons(reasons);
        return resp;
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
