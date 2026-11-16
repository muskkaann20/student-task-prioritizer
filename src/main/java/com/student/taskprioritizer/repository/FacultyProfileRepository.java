package com.student.taskprioritizer.repository;

import com.student.taskprioritizer.model.FacultyProfile;
import com.student.taskprioritizer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {

    Optional<FacultyProfile> findByUser(User user);
}
