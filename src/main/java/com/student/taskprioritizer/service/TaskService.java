package com.student.taskprioritizer.service;

import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Business logic for task CRUD and priority calculation.
 *
 * Priority algorithm
 * ──────────────────
 * 1. Deadline is the primary ranking key for incomplete tasks.
 *    A sooner due date always ranks above a later one, regardless of task type.
 *    Overdue tasks rank before future tasks; the most overdue task is first.
 *
 * 2. Days remaining uses calendar dates in the JVM default time zone
 *    (matching SQLite epoch-millis values stored at local midnight):
 *      daysRemaining = ChronoUnit.DAYS.between(today, dueDate)
 *
 * 3. Urgency increases as the deadline approaches:
 *      urgency = 100 − daysRemaining
 *    Overdue tasks have daysRemaining &lt; 0, so urgency &gt; 100.
 *
 * 4. Each task type has a secondary weight (used only as a tie-breaker):
 *      PLACEMENT_TEST → 4
 *      EXAM           → 3
 *      PROJECT        → 2
 *      ASSIGNMENT     → 1
 *
 * 5. Priority score = urgency + (typeWeight × 0.1)
 *    One day of deadline difference changes the score by 1.0.
 *    Type weight adds at most 0.4, so it cannot override a nearer deadline.
 *
 * 6. Results are ordered by days remaining ascending, then type weight
 *    descending when deadlines are equal.
 */
@Service
public class TaskService {

    private final TaskRepository repository;

    private static final Map<String, Integer> TYPE_WEIGHT = Map.of(
            "PLACEMENT_TEST", 4,
            "EXAM", 3,
            "PROJECT", 2,
            "ASSIGNMENT", 1
    );

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    // ---------- CRUD ----------

    public Task createTask(Task task) {
        return repository.save(task);
    }

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return repository.findById(id);
    }

    public Task updateTask(Long id, Task updated) {
        Task existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));
        existing.setSubject(updated.getSubject());
        existing.setTitle(updated.getTitle());
        existing.setTaskType(updated.getTaskType());
        existing.setDueDate(updated.getDueDate());
        existing.setDescription(updated.getDescription());
        existing.setCompleted(updated.isCompleted());
        return repository.save(existing);
    }

    public void deleteTask(Long id) {
        repository.deleteById(id);
    }

    public Task markComplete(Long id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));
        task.setCompleted(true);
        return repository.save(task);
    }

    // ---------- Priority ----------

    /**
     * Returns incomplete tasks in priority order (nearest / most overdue first).
     * Completed tasks are excluded. Each map contains the task, priority score,
     * days remaining, and a human-readable urgency label.
     */
    public List<Map<String, Object>> getPrioritizedTasks() {
        List<Task> pending = repository.findByCompletedFalseOrderByDueDateAsc()
                .stream()
                .filter(task -> !task.isCompleted())
                .toList();
        LocalDate today = LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Task task : pending) {
            long daysRemaining = ChronoUnit.DAYS.between(today, task.getDueDate());
            double urgency = 100.0 - daysRemaining;
            int weight = TYPE_WEIGHT.getOrDefault(task.getTaskType(), 1);
            double priorityScore = urgency + (weight * 0.1);

            String urgencyLabel;
            if (daysRemaining < 0) {
                urgencyLabel = "OVERDUE";
            } else if (daysRemaining == 0) {
                urgencyLabel = "DUE TODAY";
            } else if (daysRemaining <= 2) {
                urgencyLabel = "URGENT";
            } else if (daysRemaining <= 7) {
                urgencyLabel = "HIGH";
            } else if (daysRemaining <= 14) {
                urgencyLabel = "MEDIUM";
            } else {
                urgencyLabel = "LOW";
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("task", task);
            entry.put("priorityScore", Math.round(priorityScore * 100.0) / 100.0);
            entry.put("daysRemaining", daysRemaining);
            entry.put("urgencyLabel", urgencyLabel);
            result.add(entry);
        }

        result.sort(Comparator
                .comparingLong((Map<String, Object> e) -> ((Number) e.get("daysRemaining")).longValue())
                .thenComparingInt(e -> -TYPE_WEIGHT.getOrDefault(((Task) e.get("task")).getTaskType(), 1))
                .thenComparingLong(e -> {
                    Long id = ((Task) e.get("task")).getId();
                    return id == null ? Long.MAX_VALUE : id;
                }));

        return result;
    }
}
