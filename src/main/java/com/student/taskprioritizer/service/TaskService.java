package com.student.taskprioritizer.service;

import com.student.taskprioritizer.dto.CreateTaskRequest;
import com.student.taskprioritizer.model.*;
import com.student.taskprioritizer.repository.SkillRepository;
import com.student.taskprioritizer.repository.TaskRepository;
import com.student.taskprioritizer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    private static final Map<TaskType, Integer> TYPE_WEIGHT = Map.of(
            TaskType.PLACEMENT_TEST, 4,
            TaskType.EXAM, 3,
            TaskType.PROJECT, 2,
            TaskType.ASSIGNMENT, 1
    );

    public TaskService(TaskRepository repository,
                       SkillRepository skillRepository,
                       UserRepository userRepository) {
        this.repository = repository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    public Task createTask(Task task, String ownerId) {
        task.setId(null);
        task.setOwnerId(ownerId);
        return repository.save(task);
    }

    @Transactional
    public Task createTask(CreateTaskRequest request, User owner) {
        Task task = new Task();
        task.setSubject(request.getSubject());
        task.setTitle(request.getTitle());
        task.setTaskType(request.getTaskType());
        task.setDueDate(request.getDueDate());
        task.setDescription(request.getDescription());
        task.setOwner(owner);
        task.setOwnerId(owner.getId().toString());
        task.setCompleted(false);
        task.setStatus(TaskStatus.PENDING);
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getRequiredSkillId() != null) {
            skillRepository.findById(request.getRequiredSkillId())
                    .ifPresent(task::setRequiredSkill);
        }
        return repository.save(task);
    }

    @Transactional
    public Task createAdminTask(CreateTaskRequest request, User admin) {
        Task task = new Task();
        task.setSubject(request.getSubject());
        task.setTitle(request.getTitle());
        task.setTaskType(request.getTaskType());
        task.setDueDate(request.getDueDate());
        task.setDescription(request.getDescription());
        task.setAssignedBy(admin);
        task.setOwner(admin);
        task.setCompleted(false);
        task.setStatus(TaskStatus.PENDING);
        task.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : 1.0);
        if (request.getRequiredSkillId() != null) {
            skillRepository.findById(request.getRequiredSkillId())
                    .ifPresent(task::setRequiredSkill);
        }
        return repository.save(task);
    }

    @Transactional
    public Task assignTask(Long taskId, UUID facultyUserId, User admin) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        User faculty = userRepository.findById(facultyUserId)
                .orElseThrow(() -> new NoSuchElementException("Faculty user not found"));
        if (faculty.getRole() != Role.FACULTY) {
            throw new IllegalArgumentException("User is not a faculty member");
        }
        task.setAssignedTo(faculty);
        task.setAssignedBy(admin);
        task.setStatus(TaskStatus.PENDING);
        task.setDeclineReason(null);
        return repository.save(task);
    }

    @Transactional
    public Task acceptTask(Long taskId, User faculty) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(faculty.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Task not assigned to you");
        }
        task.setStatus(TaskStatus.ACCEPTED);
        task.setDeclineReason(null);
        return repository.save(task);
    }

    @Transactional
    public Task declineTask(Long taskId, String reason, User faculty) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(faculty.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Task not assigned to you");
        }
        if (reason == null || reason.trim().isBlank()) {
            throw new IllegalArgumentException("Declining requires a reason");
        }
        task.setStatus(TaskStatus.DECLINED);
        task.setDeclineReason(reason.trim());
        return repository.save(task);
    }

    public List<Task> getAssignedTasks(User faculty) {
        return repository.findByAssignedTo(faculty);
    }

    public List<Task> getAllTasksForAdmin() {
        return repository.findAll();
    }

    public List<Task> getAllTasks(String ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    public List<Task> getAllTasks(User owner) {
        return repository.findByOwner(owner);
    }

    public Optional<Task> getTaskById(Long id) {
        return repository.findById(id);
    }

    public Optional<Task> getTaskById(Long id, String ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId);
    }

    public Optional<Task> getTaskById(Long id, User owner) {
        return repository.findByIdAndOwner(id, owner);
    }

    public Task updateTask(Long id, Task updated, String ownerId) {
        Task existing = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        applyUpdate(existing, updated);
        return repository.save(existing);
    }

    public Task updateTask(Long id, Task updated, User owner) {
        Task existing = repository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        applyUpdate(existing, updated);
        return repository.save(existing);
    }

    private void applyUpdate(Task existing, Task updated) {
        existing.setSubject(updated.getSubject());
        existing.setTitle(updated.getTitle());
        existing.setTaskType(updated.getTaskType());
        existing.setDueDate(updated.getDueDate());
        existing.setDescription(updated.getDescription());
        if (updated.getEstimatedHours() != null) {
            existing.setEstimatedHours(updated.getEstimatedHours());
        }
        if (updated.getRequiredSkill() != null) {
            existing.setRequiredSkill(updated.getRequiredSkill());
        }
    }

    public void deleteTask(Long id, String ownerId) {
        Task task = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        repository.delete(task);
    }

    public void deleteTask(Long id, User owner) {
        Task task = repository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        repository.delete(task);
    }

    public Task markComplete(Long id, String ownerId) {
        Task task = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        task.setCompleted(true);
        task.setStatus(TaskStatus.DONE);
        return repository.save(task);
    }

    public Task markComplete(Long id, User owner) {
        Task task = repository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        task.setCompleted(true);
        task.setStatus(TaskStatus.DONE);
        return repository.save(task);
    }

    public List<Map<String, Object>> getPrioritizedTasks(String ownerId) {
        return rank(repository.findByCompletedFalseAndOwnerIdOrderByDueDateAsc(ownerId));
    }

    public List<Map<String, Object>> getPrioritizedTasks(User owner) {
        return rank(repository.findByCompletedFalseAndOwnerOrderByDueDateAsc(owner));
    }

    private List<Map<String, Object>> rank(List<Task> pending) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Task task : pending) {
            if (task.isCompleted()) {
                continue;
            }
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
