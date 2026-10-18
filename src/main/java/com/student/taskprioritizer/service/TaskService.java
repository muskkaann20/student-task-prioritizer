
package com.student.taskprioritizer.service;

import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    public Task createTask(Task task, String ownerId) {
        task.setId(null);
        task.setOwnerId(ownerId);
        return repository.save(task);
    }

    public List<Task> getAllTasks(String ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    public Optional<Task> getTaskById(Long id, String ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId);
    }

    public Task updateTask(Long id, Task updated, String ownerId) {
        Task existing = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));

        existing.setSubject(updated.getSubject());
        existing.setTitle(updated.getTitle());
        existing.setTaskType(updated.getTaskType());
        existing.setDueDate(updated.getDueDate());
        existing.setDescription(updated.getDescription());
        return repository.save(existing);
    }

    public void deleteTask(Long id, String ownerId) {
        Task task = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        repository.delete(task);
    }

    public Task markComplete(Long id, String ownerId) {
        Task task = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        task.setCompleted(true);
        return repository.save(task);
    }

    public List<Map<String, Object>> getPrioritizedTasks(String ownerId) {
        List<Task> pending = repository
                .findByCompletedFalseAndOwnerIdOrderByDueDateAsc(ownerId);

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Task task : pending) {
            long daysRemaining = ChronoUnit.DAYS.between(today, task.getDueDate());
            double urgency = 100.0 - daysRemaining;
            int weight = TYPE_WEIGHT.getOrDefault(task.getTaskType(), 1);
            double priorityScore = urgency + weight * 0.1;

            String urgencyLabel;
            if (daysRemaining < 0) urgencyLabel = "OVERDUE";
            else if (daysRemaining == 0) urgencyLabel = "DUE TODAY";
            else if (daysRemaining <= 2) urgencyLabel = "URGENT";
            else if (daysRemaining <= 7) urgencyLabel = "HIGH";
            else if (daysRemaining <= 14) urgencyLabel = "MEDIUM";
            else urgencyLabel = "LOW";

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("task", task);
            entry.put("priorityScore", Math.round(priorityScore * 100.0) / 100.0);
            entry.put("daysRemaining", daysRemaining);
            entry.put("urgencyLabel", urgencyLabel);
            result.add(entry);
        }

        result.sort(Comparator
                .comparingLong((Map<String, Object> e) ->
                        ((Number) e.get("daysRemaining")).longValue())
                .thenComparingInt(e -> -TYPE_WEIGHT.getOrDefault(
                        ((Task) e.get("task")).getTaskType(), 1))
                .thenComparingLong(e -> {
                    Long id = ((Task) e.get("task")).getId();
                    return id == null ? Long.MAX_VALUE : id;
                }));

        return result;
    }
}
