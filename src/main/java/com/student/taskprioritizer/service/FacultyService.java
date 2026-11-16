package com.student.taskprioritizer.service;

import com.student.taskprioritizer.dto.CreateFacultyRequest;
import com.student.taskprioritizer.dto.FacultyProfileResponse;
import com.student.taskprioritizer.dto.SkillProficiencyDto;
import com.student.taskprioritizer.model.*;
import com.student.taskprioritizer.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FacultyService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyProfileRepository profileRepository;
    private final FacultySkillRepository facultySkillRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkloadService workloadService;

    public FacultyService(UserRepository userRepository,
                          DepartmentRepository departmentRepository,
                          FacultyProfileRepository profileRepository,
                          FacultySkillRepository facultySkillRepository,
                          SkillRepository skillRepository,
                          PasswordEncoder passwordEncoder,
                          WorkloadService workloadService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.profileRepository = profileRepository;
        this.facultySkillRepository = facultySkillRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
        this.workloadService = workloadService;
    }

    @Transactional
    public FacultyProfileResponse createFaculty(CreateFacultyRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account already exists for that email");
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NoSuchElementException("Department not found"));

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.FACULTY);
        user.setDepartment(department);
        userRepository.save(user);

        FacultyProfile profile = new FacultyProfile();
        profile.setUser(user);
        profile.setDesignation(request.getDesignation().trim());
        profile.setMaxHoursPerWeek(request.getMaxHoursPerWeek());
        profileRepository.save(profile);

        replaceSkills(profile, request.getSkills());
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<FacultyProfileResponse> listFaculty() {
        return profileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FacultyProfileResponse getByUser(User user) {
        FacultyProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NoSuchElementException("Faculty profile not found"));
        return toResponse(profile);
    }

    private void replaceSkills(FacultyProfile profile, List<SkillProficiencyDto> skills) {
        facultySkillRepository.deleteAll(facultySkillRepository.findByFacultyProfile(profile));
        if (skills == null) return;
        for (SkillProficiencyDto dto : skills) {
            Skill skill = skillRepository.findByName(dto.getSkillName().trim())
                    .orElseGet(() -> skillRepository.save(new Skill(dto.getSkillName().trim())));
            facultySkillRepository.save(new FacultySkill(profile, skill, dto.getProficiency()));
        }
    }

    public FacultyProfileResponse toResponse(FacultyProfile profile) {
        User user = profile.getUser();
        FacultyProfileResponse resp = new FacultyProfileResponse();
        resp.setUserId(user.getId());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());
        resp.setDesignation(profile.getDesignation());
        resp.setDepartment(user.getDepartment() == null ? null : user.getDepartment().getName());
        resp.setMaxHoursPerWeek(profile.getMaxHoursPerWeek());
        resp.setCurrentLoadHours(workloadService.currentWeekLoadHours(user));
        List<SkillProficiencyDto> skills = new ArrayList<>();
        for (FacultySkill fs : facultySkillRepository.findByFacultyProfile(profile)) {
            skills.add(new SkillProficiencyDto(fs.getSkill().getName(), fs.getProficiency()));
        }
        resp.setSkills(skills);
        return resp;
    }
}
