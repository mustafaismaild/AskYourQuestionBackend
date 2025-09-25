package com.example.project.repository;

import com.example.project.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdAndStatus(Long questionId, boolean status);
}
