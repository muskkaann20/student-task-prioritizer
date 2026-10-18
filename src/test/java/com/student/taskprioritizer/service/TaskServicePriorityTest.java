package com.student.taskprioritizer.service;

import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServicePriorityTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    @Test
    void ranksByNearestDeadlineRegardlessOfTaskType() {
        Task placementIn13 = task(1L, "PLACEMENT_TEST", today.plusDays(13), false);
        Task examIn5 = task(2L, "EXAM", today.plusDays(5), false);
        Task projectIn2 = task(3L, "PROJECT", today.plusDays(2), false);
        Task assignmentIn1 = task(4L, "ASSIGNMENT", today.plusDays(1), false);

        when(repository.findByCompletedFalseOrderByDueDateAsc())
                .thenReturn(List.of(placementIn13, examIn5, projectIn2, assignmentIn1));

        List<Map<String, Object>> ranked = service.getPrioritizedTasks();

        assertEquals(List.of(4L, 3L, 2L, 1L), ids(ranked));
        assertEquals(List.of(1L, 2L, 5L, 13L), days(ranked));
    }

    @Test
    void overdueTasksAppearBeforeFutureTasksWithMostOverdueFirst() {
        Task futureExam = task(1L, "EXAM", today.plusDays(2), false);
        Task overdueAssignment = task(2L, "ASSIGNMENT", today.minusDays(1), false);
        Task moreOverdueProject = task(3L, "PROJECT", today.minusDays(4), false);
        Task dueToday = task(4L, "PLACEMENT_TEST", today, false);

        when(repository.findByCompletedFalseOrderByDueDateAsc())
                .thenReturn(List.of(futureExam, overdueAssignment, moreOverdueProject, dueToday));

        List<Map<String, Object>> ranked = service.getPrioritizedTasks();

        assertEquals(List.of(3L, 2L, 4L, 1L), ids(ranked));
        assertEquals(-4L, days(ranked).get(0));
        assertEquals(-1L, days(ranked).get(1));
        assertEquals(0L, days(ranked).get(2));
        assertEquals(2L, days(ranked).get(3));
        assertEquals("OVERDUE", ranked.get(0).get("urgencyLabel"));
        assertEquals("DUE TODAY", ranked.get(2).get("urgencyLabel"));
    }

    @Test
    void equalDeadlinesUseTaskTypeAsTieBreaker() {
        LocalDate sameDay = today.plusDays(3);
        Task assignment = task(1L, "ASSIGNMENT", sameDay, false);
        Task project = task(2L, "PROJECT", sameDay, false);
        Task exam = task(3L, "EXAM", sameDay, false);
        Task placement = task(4L, "PLACEMENT_TEST", sameDay, false);

        when(repository.findByCompletedFalseOrderByDueDateAsc())
                .thenReturn(List.of(assignment, project, exam, placement));

        List<Map<String, Object>> ranked = service.getPrioritizedTasks();

        assertEquals(List.of(4L, 3L, 2L, 1L), ids(ranked));
        assertTrue(ranked.stream().allMatch(e -> ((Number) e.get("daysRemaining")).longValue() == 3L));
        assertTrue((double) ranked.get(0).get("priorityScore")
                > (double) ranked.get(3).get("priorityScore"));
    }

    @Test
    void completedTasksAreExcludedFromPriorityRecommendations() {
        Task active = task(1L, "EXAM", today.plusDays(1), false);
        Task completedSooner = task(2L, "ASSIGNMENT", today.minusDays(2), true);
        Task completedSameDay = task(3L, "PROJECT", today.plusDays(1), true);

        when(repository.findByCompletedFalseOrderByDueDateAsc())
                .thenReturn(List.of(active, completedSooner, completedSameDay));

        List<Map<String, Object>> ranked = service.getPrioritizedTasks();

        assertEquals(List.of(1L), ids(ranked));
        assertTrue(ranked.stream().noneMatch(e -> ((Task) e.get("task")).isCompleted()));
    }

    @Test
    void urgencyScoreIncreasesAsDeadlineApproaches() {
        Task in13 = task(1L, "ASSIGNMENT", today.plusDays(13), false);
        Task in5 = task(2L, "ASSIGNMENT", today.plusDays(5), false);
        Task in2 = task(3L, "ASSIGNMENT", today.plusDays(2), false);
        Task in1 = task(4L, "ASSIGNMENT", today.plusDays(1), false);

        when(repository.findByCompletedFalseOrderByDueDateAsc())
                .thenReturn(List.of(in13, in5, in2, in1));

        List<Map<String, Object>> ranked = service.getPrioritizedTasks();

        double score1 = (double) ranked.get(0).get("priorityScore");
        double score2 = (double) ranked.get(1).get("priorityScore");
        double score5 = (double) ranked.get(2).get("priorityScore");
        double score13 = (double) ranked.get(3).get("priorityScore");

        assertTrue(score1 > score2);
        assertTrue(score2 > score5);
        assertTrue(score5 > score13);
        assertEquals(99.1, score1, 0.001);
        assertEquals(98.1, score2, 0.001);
        assertEquals(95.1, score5, 0.001);
        assertEquals(87.1, score13, 0.001);
    }

    @Test
    void nearerAssignmentOutranksLaterPlacementTest() {
        Task assignmentTomorrow = task(1L, "ASSIGNMENT", today.plusDays(1), false);
        Task placementInFive = task(2L, "PLACEMENT_TEST", today.plusDays(5), false);

        when(repository.findByCompletedFalseOrderByDueDateAsc())
                .thenReturn(List.of(placementInFive, assignmentTomorrow));

        List<Map<String, Object>> ranked = service.getPrioritizedTasks();

        assertEquals(1L, ((Task) ranked.get(0).get("task")).getId());
        assertTrue((double) ranked.get(0).get("priorityScore")
                > (double) ranked.get(1).get("priorityScore"));
    }

    private static Task task(Long id, String type, LocalDate due, boolean completed) {
        Task task = new Task("Subject", type + "-" + id, type, due, "");
        task.setId(id);
        task.setCompleted(completed);
        return task;
    }

    private static List<Long> ids(List<Map<String, Object>> ranked) {
        return ranked.stream().map(e -> ((Task) e.get("task")).getId()).toList();
    }

    private static List<Long> days(List<Map<String, Object>> ranked) {
        return ranked.stream().map(e -> ((Number) e.get("daysRemaining")).longValue()).toList();
    }
}
