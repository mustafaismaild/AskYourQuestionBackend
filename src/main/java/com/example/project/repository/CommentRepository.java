package com.example.project.repository;

import com.example.project.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByQuestionIdAndStatus(Long questionId, boolean status);
    List<Comment> findByAnswerIdAndStatus(Long answerId, boolean status);
    Optional<Comment> findByIdAndStatus(Long id, boolean status);
    List<Comment> findByUserIdAndStatus(Long userId, boolean status);
}
