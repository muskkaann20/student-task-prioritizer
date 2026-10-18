
package com.student.taskprioritizer.controller;

import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private String getOwnerId(String ownerId) {
        if (ownerId == null || !ownerId.matches("[a-zA-Z0-9-]{16,100}")) {
            throw new IllegalArgumentException("Invalid owner ID");
        }
        return ownerId;
    }

    @PostMapping
    public ResponseEntity<Task> create(
            @RequestHeader("X-Owner-Id") String ownerId,
            @RequestBody Task task) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(task, getOwnerId(ownerId)));
    }

    @GetMapping
    public List<Task> getAll(@RequestHeader("X-Owner-Id") String ownerId) {
        return taskService.getAllTasks(getOwnerId(ownerId));
    }

    @GetMapping("/prioritized")
    public List<Map<String, Object>> getPrioritized(
            @RequestHeader("X-Owner-Id") String ownerId) {
        return taskService.getPrioritizedTasks(getOwnerId(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return taskService.getTaskById(id, getOwnerId(ownerId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") String ownerId,
            @RequestBody Task task) {
        try {
            return ResponseEntity.ok(
                    taskService.updateTask(id, task, getOwnerId(ownerId)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> markComplete(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") String ownerId) {
        try {
            return ResponseEntity.ok(
                    taskService.markComplete(id, getOwnerId(ownerId)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") String ownerId) {
        try {
            taskService.deleteTask(id, getOwnerId(ownerId));
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
