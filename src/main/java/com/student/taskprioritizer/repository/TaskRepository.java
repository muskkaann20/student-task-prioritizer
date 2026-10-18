package com.student.taskprioritizer.repository;

import com.student.taskprioritizer.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** All incomplete tasks ordered by due date (soonest first). */
    List<Task> findByCompletedFalseOrderByDueDateAsc();

    /** All tasks for a specific subject. */
    List<Task> findBySubjectIgnoreCase(String subject);

    /** All completed tasks. */
    List<Task> findByCompletedTrue();
}
