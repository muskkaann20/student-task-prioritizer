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

    /** Create a new task. */
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task));
    }

    /** Get all tasks (unordered). */
    @GetMapping
    public List<Task> getAll() {
        return taskService.getAllTasks();
    }

    /** Get tasks sorted by priority (highest first). */
    @GetMapping("/prioritized")
    public List<Map<String, Object>> getPrioritized() {
        return taskService.getPrioritizedTasks();
    }

    /** Get a single task by id. */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Update a task. */
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.updateTask(id, task));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Mark a task as completed. */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> markComplete(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.markComplete(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Delete a task. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
