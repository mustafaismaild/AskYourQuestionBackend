package com.example.project.repository;

import com.example.project.entity.SavedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedQuestionRepository extends JpaRepository<SavedQuestion, Long> {
    List<SavedQuestion> findByUserId(Long userId);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
}