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

    @Query("SELECT DISTINCT q FROM Question q " +
            "JOIN q.tags t " +
            "WHERE t.name = :tagName AND q.status = true")
    List<Question> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT DISTINCT q FROM Question q " +
            "JOIN q.tags t " +
            "WHERE t.id = :tagId AND q.status = true")
    List<Question> findByTagId(@Param("tagId") Long tagId);

    @Query("SELECT q FROM Question q WHERE q.user.id = :userId AND q.status = :status")
    List<Question> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") boolean status);

    // Gelişmiş arama ve sıralama
    @Query("SELECT q FROM Question q WHERE q.status = true ORDER BY q.viewCount DESC")
    List<Question> findMostViewed();

    @Query("SELECT q FROM Question q WHERE q.status = true ORDER BY q.createdAt DESC")
    List<Question> findLatest();

    @Query("SELECT q FROM Question q WHERE q.status = true AND q.isSolved = false ORDER BY q.createdAt DESC")
    List<Question> findUnanswered();

    @Query("SELECT q FROM Question q WHERE q.status = true AND q.isSolved = true ORDER BY q.updatedAt DESC")
    List<Question> findRecentlySolved();

    // Full-text search (MySQL için)
    @Query(value = "SELECT * FROM questions WHERE MATCH(title, content) AGAINST(:searchTerm IN NATURAL LANGUAGE MODE) AND status = true", 
           nativeQuery = true)
    List<Question> fullTextSearch(@Param("searchTerm") String searchTerm);

    // View count artırma
    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.viewCount = q.viewCount + 1 WHERE q.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
