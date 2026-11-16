
package com.student.taskprioritizer.repository;

import com.student.taskprioritizer.model.Task;
import com.student.taskprioritizer.model.TaskStatus;
import com.student.taskprioritizer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ── Legacy queries (used only by existing data; ownerId = localStorage UUID) ──

    List<Task> findByOwnerId(String ownerId);

    Optional<Task> findByIdAndOwnerId(Long id, String ownerId);

    List<Task> findByCompletedFalseAndOwnerIdOrderByDueDateAsc(String ownerId);

    List<Task> findBySubjectIgnoreCase(String subject);

    List<Task> findByCompletedTrue();

    // ── Phase-1 queries — keyed by the new User FK (owner column / user_id) ──

    List<Task> findByOwner(User owner);

    Optional<Task> findByIdAndOwner(Long id, User owner);

    List<Task> findByCompletedFalseAndOwnerOrderByDueDateAsc(User owner);

    // ── Faculty queries ──

    List<Task> findByAssignedTo(User assignedTo);

    List<Task> findByAssignedToAndStatusIn(User assignedTo, List<TaskStatus> statuses);

    /**
     * Tasks assigned to a faculty member whose due date falls within a given
     * week, filtered by status. The {@code dueDate} field uses
     * {@link com.student.taskprioritizer.model.LocalDateEpochConverter}, so JPA
     * converts the {@link LocalDate} parameters to epoch-millis before querying.
     */
    List<Task> findByAssignedToAndDueDateBetweenAndStatusIn(
            User assignedTo, LocalDate dueDateStart, LocalDate dueDateEnd,
            List<TaskStatus> statuses);

    // ── Admin queries ──

    List<Task> findByAssignedToIsNotNull();

    // ── Migration helpers ──

    /** Back-fills {@code status = PENDING} for any migrated row that has no status. */
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = :status WHERE t.status IS NULL")
    void backfillStatus(@Param("status") TaskStatus status);

    /** Back-fills {@code owner} for migrated rows that have no User FK yet. */
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.owner = :owner WHERE t.owner IS NULL")
    void backfillOwner(@Param("owner") User owner);
}
