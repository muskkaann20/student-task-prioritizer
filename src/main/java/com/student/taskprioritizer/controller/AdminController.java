package com.student.taskprioritizer.controller;

import com.student.taskprioritizer.dto.*;
import com.student.taskprioritizer.model.Department;
import com.student.taskprioritizer.model.Skill;
import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.model.User;
import com.student.taskprioritizer.repository.DepartmentRepository;
import com.student.taskprioritizer.repository.SkillRepository;
import com.student.taskprioritizer.security.CurrentUserService;
import com.student.taskprioritizer.service.FacultyService;
import com.student.taskprioritizer.service.SuggestionService;
import com.student.taskprioritizer.service.TaskService;
import com.student.taskprioritizer.service.WorkloadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FacultyService facultyService;
    private final TaskService taskService;
    private final SuggestionService suggestionService;
    private final WorkloadService workloadService;
    private final DepartmentRepository departmentRepository;
    private final SkillRepository skillRepository;
    private final CurrentUserService currentUserService;

    public AdminController(FacultyService facultyService,
                           TaskService taskService,
                           SuggestionService suggestionService,
                           WorkloadService workloadService,
                           DepartmentRepository departmentRepository,
                           SkillRepository skillRepository,
                           CurrentUserService currentUserService) {
        this.facultyService = facultyService;
        this.taskService = taskService;
        this.suggestionService = suggestionService;
        this.workloadService = workloadService;
        this.departmentRepository = departmentRepository;
        this.skillRepository = skillRepository;
        this.currentUserService = currentUserService;
    }

    private User admin() {
        return currentUserService.requireUser();
    }

    // ── Thymeleaf Web Views ──────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "suggestTaskId", required = false) Long suggestTaskId,
                            Model model) {
        populateDashboardModel(model);

        if (suggestTaskId != null) {
            taskService.getTaskById(suggestTaskId).ifPresent(task -> {
                model.addAttribute("selectedTask", TaskResponse.from(task));
                model.addAttribute("suggestions", suggestionService.suggest(task));
            });
        }
        return "admin/dashboard";
    }

    private void populateDashboardModel(Model model) {
        model.addAttribute("adminUser", admin());
        model.addAttribute("facultyList", facultyService.listFaculty());
        model.addAttribute("workloadList", workloadService.getWorkloadSummary());
        model.addAttribute("taskList", taskService.getAllTasksForAdmin().stream()
                .map(TaskResponse::from).toList());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("skills", skillRepository.findAll());
        if (!model.containsAttribute("createFacultyRequest")) {
            model.addAttribute("createFacultyRequest", new CreateFacultyRequest());
        }
        if (!model.containsAttribute("createTaskRequest")) {
            model.addAttribute("createTaskRequest", new CreateTaskRequest());
        }
    }

    @PostMapping("/faculty")
    public String createFacultyForm(@Valid @ModelAttribute("createFacultyRequest") CreateFacultyRequest request,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            populateDashboardModel(model);
            model.addAttribute("formError", "Please fill in all required faculty fields (Name, Email, Password, Department, Designation, Max Hours).");
            return "admin/dashboard";
        }
        try {
            facultyService.createFaculty(request);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty account created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/tasks")
    public String createTaskForm(@Valid @ModelAttribute("createTaskRequest") CreateTaskRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            populateDashboardModel(model);
            model.addAttribute("formError", "Please select a valid Task Type and fill in Subject, Title, and Due Date.");
            return "admin/dashboard";
        }
        try {
            Task task = taskService.createAdminTask(request, admin());
            redirectAttributes.addFlashAttribute("successMessage", "Task created. Select faculty to assign.");
            return "redirect:/admin/dashboard?suggestTaskId=" + task.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/tasks/{id}/assign")
    public String assignTaskForm(@PathVariable Long id,
                                 @RequestParam("facultyUserId") UUID facultyUserId,
                                 RedirectAttributes redirectAttributes) {
        try {
            taskService.assignTask(id, facultyUserId, admin());
            redirectAttributes.addFlashAttribute("successMessage", "Task assigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // ── REST APIs ────────────────────────────────────────────────────

    @ResponseBody
    @PostMapping("/api/faculty")
    public ResponseEntity<FacultyProfileResponse> createFacultyApi(@Valid @RequestBody CreateFacultyRequest request) {
        FacultyProfileResponse created = facultyService.createFaculty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @ResponseBody
    @GetMapping("/api/faculty")
    public List<FacultyProfileResponse> listFacultyApi() {
        return facultyService.listFaculty();
    }

    @ResponseBody
    @GetMapping("/api/workload")
    public List<WorkloadEntry> listWorkloadApi() {
        return workloadService.getWorkloadSummary();
    }

    @ResponseBody
    @PostMapping("/api/tasks")
    public ResponseEntity<TaskResponse> createTaskApi(@Valid @RequestBody CreateTaskRequest request) {
        Task created = taskService.createAdminTask(request, admin());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(created));
    }

    @ResponseBody
    @GetMapping("/api/tasks")
    public List<TaskResponse> listTasksApi() {
        return taskService.getAllTasksForAdmin().stream()
                .map(TaskResponse::from)
                .toList();
    }

    @ResponseBody
    @GetMapping("/api/tasks/{id}/suggestions")
    public ResponseEntity<List<FacultySuggestionResponse>> suggestionsApi(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(task -> ResponseEntity.ok(suggestionService.suggest(task)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ResponseBody
    @PostMapping("/api/tasks/{id}/assign")
    public ResponseEntity<TaskResponse> assignTaskApi(@PathVariable Long id,
                                                      @Valid @RequestBody AssignTaskRequest request) {
        Task assigned = taskService.assignTask(id, request.getFacultyUserId(), admin());
        return ResponseEntity.ok(TaskResponse.from(assigned));
    }
}
