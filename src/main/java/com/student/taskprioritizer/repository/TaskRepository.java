
package com.student.taskprioritizer.repository;

import com.student.taskprioritizer.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerId(String ownerId);

    Optional<Task> findByIdAndOwnerId(Long id, String ownerId);

    List<Task> findByCompletedFalseAndOwnerIdOrderByDueDateAsc(
            String ownerId);

    List<Task> findBySubjectIgnoreCase(String subject);

    List<Task> findByCompletedTrue();
}
