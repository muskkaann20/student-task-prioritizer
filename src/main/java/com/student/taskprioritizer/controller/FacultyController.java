package com.student.taskprioritizer.controller;

import com.student.taskprioritizer.dto.DeclineTaskRequest;
import com.student.taskprioritizer.dto.FacultyProfileResponse;
import com.student.taskprioritizer.dto.TaskResponse;
import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.model.User;
import com.student.taskprioritizer.security.CurrentUserService;
import com.student.taskprioritizer.service.FacultyService;
import com.student.taskprioritizer.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyService facultyService;
    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    public FacultyController(FacultyService facultyService,
                             TaskService taskService,
                             CurrentUserService currentUserService) {
        this.facultyService = facultyService;
        this.taskService = taskService;
        this.currentUserService = currentUserService;
    }

    private User faculty() {
        return currentUserService.requireUser();
    }

    // ── Thymeleaf Web Views ──────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = faculty();
        FacultyProfileResponse profile = facultyService.getByUser(user);
        List<TaskResponse> tasks = taskService.getAssignedTasks(user).stream()
                .map(TaskResponse::from)
                .toList();

        model.addAttribute("facultyUser", user);
        model.addAttribute("profile", profile);
        model.addAttribute("tasks", tasks);
        return "faculty/dashboard";
    }

    @PostMapping("/tasks/{id}/accept")
    public String acceptTaskForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            taskService.acceptTask(id, faculty());
            redirectAttributes.addFlashAttribute("successMessage", "Task accepted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/tasks/{id}/decline")
    public String declineTaskForm(@PathVariable Long id,
                                  @RequestParam("reason") String reason,
                                  RedirectAttributes redirectAttributes) {
        try {
            taskService.declineTask(id, reason, faculty());
            redirectAttributes.addFlashAttribute("successMessage", "Task declined.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/dashboard";
    }

    // ── REST APIs ────────────────────────────────────────────────────

    @ResponseBody
    @GetMapping("/api/profile")
    public FacultyProfileResponse profileApi() {
        return facultyService.getByUser(faculty());
    }

    @ResponseBody
    @GetMapping("/api/tasks")
    public List<TaskResponse> tasksApi() {
        return taskService.getAssignedTasks(faculty()).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @ResponseBody
    @PostMapping("/api/tasks/{id}/accept")
    public ResponseEntity<TaskResponse> acceptTaskApi(@PathVariable Long id) {
        Task accepted = taskService.acceptTask(id, faculty());
        return ResponseEntity.ok(TaskResponse.from(accepted));
    }

    @ResponseBody
    @PostMapping("/api/tasks/{id}/decline")
    public ResponseEntity<TaskResponse> declineTaskApi(@PathVariable Long id,
                                                       @Valid @RequestBody DeclineTaskRequest request) {
        Task declined = taskService.declineTask(id, request.getReason(), faculty());
        return ResponseEntity.ok(TaskResponse.from(declined));
    }
}
