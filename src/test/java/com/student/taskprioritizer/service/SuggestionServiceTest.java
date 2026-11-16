package com.student.taskprioritizer.service;

import com.student.taskprioritizer.config.SuggestionProperties;
import com.student.taskprioritizer.dto.FacultySuggestionResponse;
import com.student.taskprioritizer.model.*;
import com.student.taskprioritizer.repository.FacultyProfileRepository;
import com.student.taskprioritizer.repository.FacultySkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private FacultyProfileRepository profileRepository;

    @Mock
    private FacultySkillRepository facultySkillRepository;

    @Mock
    private WorkloadService workloadService;

    private SuggestionProperties weights;

    private SuggestionService suggestionService;

    private Skill javaSkill;
    private Skill dbSkill;
    private LocalDate dueDate;

    @BeforeEach
    void setUp() {
        weights = new SuggestionProperties();
        weights.setProficiency(3.0);
        weights.setFreeHours(1.0);
        weights.setCurrentLoad(2.0);

        suggestionService = new SuggestionService(
                profileRepository,
                facultySkillRepository,
                workloadService,
                weights
        );

        javaSkill = new Skill("Java");
        javaSkill.setId(1L);

        dbSkill = new Skill("Databases");
        dbSkill.setId(2L);

        dueDate = LocalDate.now().plusDays(3);
    }

    private FacultyProfile createProfile(String name, int maxHours) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(name.toLowerCase().replaceAll("\\s+", "") + "@college.edu");
        user.setRole(Role.FACULTY);

        FacultyProfile profile = new FacultyProfile();
        profile.setId((long) name.hashCode());
        profile.setUser(user);
        profile.setDesignation("Assistant Professor");
        profile.setMaxHoursPerWeek(maxHours);
        return profile;
    }

    private Task createTask(String title, Skill skill, double estimatedHours, LocalDate due) {
        Task task = new Task("CS101", title, TaskType.ASSIGNMENT, due, "Desc");
        task.setRequiredSkill(skill);
        task.setEstimatedHours(estimatedHours);
        return task;
    }

    @Test
    void filtersOutFacultyWithoutRequiredSkill() {
        FacultyProfile facultyWithSkill = createProfile("Prof. Smith", 20);
        FacultyProfile facultyWithoutSkill = createProfile("Prof. Jones", 20);

        when(profileRepository.findAll()).thenReturn(List.of(facultyWithSkill, facultyWithoutSkill));

        when(facultySkillRepository.findByFacultyProfileAndSkill(facultyWithSkill, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(facultyWithSkill, javaSkill, 4)));
        when(facultySkillRepository.findByFacultyProfileAndSkill(facultyWithoutSkill, javaSkill))
                .thenReturn(Optional.empty());

        when(workloadService.loadHoursForWeekOf(eq(facultyWithSkill.getUser()), any(LocalDate.class)))
                .thenReturn(4.0);

        Task task = createTask("Java Lab", javaSkill, 4.0, dueDate);

        List<FacultySuggestionResponse> suggestions = suggestionService.suggest(task);

        assertEquals(1, suggestions.size());
        assertEquals("Prof. Smith", suggestions.get(0).getFacultyName());
    }

    @Test
    void filtersOutFacultyWithoutEnoughFreeHours() {
        // Prof. Overloaded has 20 max hours, 18 current load -> 2 free hours.
        // Task requires 4 hours -> should be filtered out.
        FacultyProfile overloaded = createProfile("Prof. Overloaded", 20);
        // Prof. Available has 20 max hours, 5 current load -> 15 free hours.
        FacultyProfile available = createProfile("Prof. Available", 20);

        when(profileRepository.findAll()).thenReturn(List.of(overloaded, available));

        when(facultySkillRepository.findByFacultyProfileAndSkill(overloaded, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(overloaded, javaSkill, 5)));
        when(facultySkillRepository.findByFacultyProfileAndSkill(available, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(available, javaSkill, 3)));

        when(workloadService.loadHoursForWeekOf(eq(overloaded.getUser()), any(LocalDate.class)))
                .thenReturn(18.0);
        when(workloadService.loadHoursForWeekOf(eq(available.getUser()), any(LocalDate.class)))
                .thenReturn(5.0);

        Task task = createTask("Java Project", javaSkill, 4.0, dueDate);

        List<FacultySuggestionResponse> suggestions = suggestionService.suggest(task);

        assertEquals(1, suggestions.size());
        assertEquals("Prof. Available", suggestions.get(0).getFacultyName());
        assertEquals(3, suggestions.get(0).getProficiency());
        assertEquals(15.0, suggestions.get(0).getFreeHours());
    }

    @Test
    void scoresCorrectlyAccordingToFormula() {
        // Formula: score = 3 * proficiency + 1 * (freeHours - estimatedHours) - 2 * currentLoad
        // Prof: proficiency = 4, maxHours = 20, currentLoad = 5.0, estimatedHours = 3.0
        // freeHours = 20 - 5 = 15.0
        // freeHours - estimatedHours = 15.0 - 3.0 = 12.0
        // expected score = 3 * 4 + 1 * 12.0 - 2 * 5.0 = 12 + 12 - 10 = 14.0
        FacultyProfile prof = createProfile("Prof. Math", 20);

        when(profileRepository.findAll()).thenReturn(List.of(prof));
        when(facultySkillRepository.findByFacultyProfileAndSkill(prof, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(prof, javaSkill, 4)));
        when(workloadService.loadHoursForWeekOf(eq(prof.getUser()), any(LocalDate.class)))
                .thenReturn(5.0);

        Task task = createTask("Java Exam Review", javaSkill, 3.0, dueDate);

        List<FacultySuggestionResponse> suggestions = suggestionService.suggest(task);

        assertEquals(1, suggestions.size());
        assertEquals(14.0, suggestions.get(0).getScore());
    }

    @Test
    void returnsTop3SortedByScoreDescending() {
        FacultyProfile p1 = createProfile("Prof. 1", 30);
        FacultyProfile p2 = createProfile("Prof. 2", 30);
        FacultyProfile p3 = createProfile("Prof. 3", 30);
        FacultyProfile p4 = createProfile("Prof. 4", 30);

        when(profileRepository.findAll()).thenReturn(List.of(p1, p2, p3, p4));

        // p1: prof 5, load 2 -> free 28. est 2 -> (free-est)=26. score = 3*5 + 1*26 - 2*2 = 15+26-4 = 37
        when(facultySkillRepository.findByFacultyProfileAndSkill(p1, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(p1, javaSkill, 5)));
        when(workloadService.loadHoursForWeekOf(eq(p1.getUser()), any(LocalDate.class))).thenReturn(2.0);

        // p2: prof 4, load 2 -> free 28. est 2 -> (free-est)=26. score = 3*4 + 1*26 - 2*2 = 12+26-4 = 34
        when(facultySkillRepository.findByFacultyProfileAndSkill(p2, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(p2, javaSkill, 4)));
        when(workloadService.loadHoursForWeekOf(eq(p2.getUser()), any(LocalDate.class))).thenReturn(2.0);

        // p3: prof 3, load 2 -> free 28. est 2 -> (free-est)=26. score = 3*3 + 1*26 - 2*2 = 9+26-4 = 31
        when(facultySkillRepository.findByFacultyProfileAndSkill(p3, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(p3, javaSkill, 3)));
        when(workloadService.loadHoursForWeekOf(eq(p3.getUser()), any(LocalDate.class))).thenReturn(2.0);

        // p4: prof 1, load 2 -> free 28. est 2 -> (free-est)=26. score = 3*1 + 1*26 - 2*2 = 3+26-4 = 25
        when(facultySkillRepository.findByFacultyProfileAndSkill(p4, javaSkill))
                .thenReturn(Optional.of(new FacultySkill(p4, javaSkill, 1)));
        when(workloadService.loadHoursForWeekOf(eq(p4.getUser()), any(LocalDate.class))).thenReturn(2.0);

        Task task = createTask("Task", javaSkill, 2.0, dueDate);

        List<FacultySuggestionResponse> suggestions = suggestionService.suggest(task);

        assertEquals(3, suggestions.size());
        assertEquals("Prof. 1", suggestions.get(0).getFacultyName());
        assertEquals("Prof. 2", suggestions.get(1).getFacultyName());
        assertEquals("Prof. 3", suggestions.get(2).getFacultyName());
        assertTrue(suggestions.get(0).getScore() > suggestions.get(1).getScore());
        assertTrue(suggestions.get(1).getScore() > suggestions.get(2).getScore());
    }
}
