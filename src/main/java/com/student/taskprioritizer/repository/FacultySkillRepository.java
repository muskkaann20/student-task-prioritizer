package com.student.taskprioritizer.repository;

import com.student.taskprioritizer.model.FacultyProfile;
import com.student.taskprioritizer.model.FacultySkill;
import com.student.taskprioritizer.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultySkillRepository extends JpaRepository<FacultySkill, Long> {

    List<FacultySkill> findByFacultyProfile(FacultyProfile facultyProfile);

    Optional<FacultySkill> findByFacultyProfileAndSkill(FacultyProfile facultyProfile, Skill skill);

    /** All faculty who have a given skill — used by the suggestion engine. */
    List<FacultySkill> findBySkill(Skill skill);
}
