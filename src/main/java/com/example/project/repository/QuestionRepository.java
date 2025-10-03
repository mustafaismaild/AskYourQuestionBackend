package com.example.project.repository;

import com.example.project.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByStatusTrue();

    List<Question> findByStatusTrueAndIsSolvedTrue();

    List<Question> findByStatusTrueAndIsSolvedFalse();

    @Query("SELECT q FROM Question q " +
            "WHERE (LOWER(q.title) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "   OR LOWER(q.content) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND q.status = true")
    List<Question> findByTextContainingIgnoreCase(@Param("text") String text);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.isSolved = true, q.updatedAt = CURRENT_TIMESTAMP WHERE q.id = :id")
    void markAsSolved(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.isSolved = false, q.updatedAt = CURRENT_TIMESTAMP WHERE q.id = :id")
    void markAsUnsolved(@Param("id") Long id);

    Optional<Question> findByIdAndUserId(Long id, Long userId);
}
