package com.student.taskprioritizer.controller;

import com.student.taskprioritizer.dto.CreateTaskRequest;
import com.student.taskprioritizer.dto.TaskResponse;
import com.student.taskprioritizer.dto.UpdateTaskRequest;
import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.model.User;
import com.student.taskprioritizer.security.CurrentUserService;
import com.student.taskprioritizer.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    public TaskController(TaskService taskService, CurrentUserService currentUserService) {
        this.taskService = taskService;
        this.currentUserService = currentUserService;
    }

    private User student() {
        return currentUserService.requireUser();
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        Task created = taskService.createTask(request, student());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(created));
    }

    @GetMapping
    public List<TaskResponse> getAll() {
        return taskService.getAllTasks(student()).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @GetMapping("/prioritized")
    public List<Map<String, Object>> getPrioritized() {
        List<Map<String, Object>> entries = taskService.getPrioritizedTasks(student());
        return entries.stream().map(e -> {
            Map<String, Object> copy = new LinkedHashMap<>(e);
            Object taskObj = e.get("task");
            if (taskObj instanceof Task t) {
                copy.put("task", TaskResponse.from(t));
            }
            return copy;
        }).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return taskService.getTaskById(id, student())
                .map(TaskResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        try {
            Task task = new Task();
            task.setSubject(request.getSubject());
            task.setTitle(request.getTitle());
            task.setTaskType(request.getTaskType());
            task.setDueDate(request.getDueDate());
            task.setDescription(request.getDescription());
            if (request.getEstimatedHours() != null) {
                task.setEstimatedHours(request.getEstimatedHours());
            }
            Task updated = taskService.updateTask(id, task, student());
            return ResponseEntity.ok(TaskResponse.from(updated));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> markComplete(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(TaskResponse.from(taskService.markComplete(id, student())));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            taskService.deleteTask(id, student());
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
