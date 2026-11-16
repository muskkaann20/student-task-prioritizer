package com.student.taskprioritizer.config;

import com.student.taskprioritizer.model.*;
import com.student.taskprioritizer.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds essential data on first startup:
 * <ol>
 *   <li>Admin account from ADMIN_EMAIL / ADMIN_PASSWORD env vars</li>
 *   <li>Default student account for migrating legacy tasks</li>
 *   <li>Sample departments and skills for testing</li>
 *   <li>Back-fills status and owner FK for migrated task rows</li>
 * </ol>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_STUDENT_EMAIL = "legacy-student@system.local";

    private final DepartmentRepository departmentRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(DepartmentRepository departmentRepository,
                      SkillRepository skillRepository,
                      UserRepository userRepository,
                      TaskRepository taskRepository,
                      PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Back-fill status for any migrated rows
        taskRepository.backfillStatus(TaskStatus.PENDING);

        // 2. Seed departments
        department("Computer Science");
        department("Electronics");
        department("Mechanical");
        department("Administration");

        // 3. Seed skills
        skill("Java");
        skill("Data Structures");
        skill("Databases");
        skill("Machine Learning");
        skill("Timetabling");

        // 4. Admin account from environment variables — never hardcoded
        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        if (adminEmail != null && !adminEmail.isBlank()
                && adminPassword != null && !adminPassword.isBlank()) {
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                log.info("Created admin account: {}", adminEmail);
            }
        } else {
            log.warn("ADMIN_EMAIL or ADMIN_PASSWORD not set — skipping admin account seed");
        }

        // 5. Default student account for migrating legacy tasks and testing student login
        String studentEmail = System.getenv("STUDENT_EMAIL");
        String studentPassword = System.getenv("STUDENT_PASSWORD");
        if (studentEmail == null || studentEmail.isBlank()) {
            studentEmail = "student@college.local";
        }
        if (studentPassword == null || studentPassword.isBlank()) {
            studentPassword = "StudentPassword123!";
        }
        final String effectiveStudentEmail = studentEmail;
        final String effectiveStudentPassword = studentPassword;

        User defaultStudent = userRepository.findByEmail(effectiveStudentEmail)
                .orElseGet(() -> {
                    User s = new User();
                    s.setName("Student");
                    s.setEmail(effectiveStudentEmail);
                    s.setPasswordHash(passwordEncoder.encode(effectiveStudentPassword));
                    s.setRole(Role.STUDENT);
                    return userRepository.save(s);
                });

        // 6. Back-fill owner FK for migrated tasks that have no user_id
        taskRepository.backfillOwner(defaultStudent);
        log.info("Student account ready: {}", effectiveStudentEmail);
    }

    private void department(String name) {
        departmentRepository.findByName(name)
                .orElseGet(() -> departmentRepository.save(new Department(name)));
    }

    private void skill(String name) {
        skillRepository.findByName(name)
                .orElseGet(() -> skillRepository.save(new Skill(name)));
    }
}
