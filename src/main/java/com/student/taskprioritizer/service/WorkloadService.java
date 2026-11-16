package com.student.taskprioritizer.service;

import com.student.taskprioritizer.dto.WorkloadEntry;
import com.student.taskprioritizer.model.FacultyProfile;
import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.model.TaskStatus;
import com.student.taskprioritizer.model.User;
import com.student.taskprioritizer.repository.FacultyProfileRepository;
import com.student.taskprioritizer.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes workload metrics for faculty members based on their active
 * task assignments (i.e. tasks in the {@code tasks} table with status
 * PENDING or ACCEPTED, assigned to the faculty member).
 */
@Service
public class WorkloadService {

    private static final List<TaskStatus> ACTIVE_STATUSES =
            List.of(TaskStatus.PENDING, TaskStatus.ACCEPTED);

    private final TaskRepository taskRepository;
    private final FacultyProfileRepository profileRepository;

    public WorkloadService(TaskRepository taskRepository,
                           FacultyProfileRepository profileRepository) {
        this.taskRepository = taskRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Sum of estimatedHours for all active tasks assigned to this faculty
     * member in the current week (Monday to Sunday).
     */
    public double currentWeekLoadHours(User faculty) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return taskRepository.findByAssignedToAndDueDateBetweenAndStatusIn(
                        faculty, weekStart, weekEnd, ACTIVE_STATUSES)
                .stream()
                .mapToDouble(t -> t.getEstimatedHours() == null ? 0 : t.getEstimatedHours())
                .sum();
    }

    /**
     * Load for a specific week containing {@code referenceDate}.
     */
    public double loadHoursForWeekOf(User faculty, LocalDate referenceDate) {
        LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return taskRepository.findByAssignedToAndDueDateBetweenAndStatusIn(
                        faculty, weekStart, weekEnd, ACTIVE_STATUSES)
                .stream()
                .mapToDouble(t -> t.getEstimatedHours() == null ? 0 : t.getEstimatedHours())
                .sum();
    }

    /**
     * Total load across all active tasks (any week).
     */
    public double totalActiveLoadHours(User faculty) {
        return taskRepository.findByAssignedToAndStatusIn(faculty, ACTIVE_STATUSES)
                .stream()
                .mapToDouble(t -> t.getEstimatedHours() == null ? 0 : t.getEstimatedHours())
                .sum();
    }

    /**
     * Returns a workload entry per faculty member for the admin workload view.
     */
    public List<WorkloadEntry> getWorkloadSummary() {
        List<FacultyProfile> profiles = profileRepository.findAll();
        List<WorkloadEntry> entries = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        String weekLabel = weekStart + " – " + weekStart.plusDays(6);

        for (FacultyProfile profile : profiles) {
            double hours = currentWeekLoadHours(profile.getUser());
            WorkloadEntry entry = new WorkloadEntry();
            entry.setFacultyUserId(profile.getUser().getId());
            entry.setFacultyName(profile.getUser().getName());
            entry.setWeekLabel(weekLabel);
            entry.setAssignedHours(hours);
            entry.setMaxHoursPerWeek(profile.getMaxHoursPerWeek());
            entries.add(entry);
        }
        return entries;
    }
}
