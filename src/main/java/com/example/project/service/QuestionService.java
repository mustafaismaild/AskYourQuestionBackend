package com.example.project.service;

import com.example.project.entity.Question;
import com.example.project.entity.req.QuestionRequest;
import com.example.project.entity.res.QuestionResponse;

import java.util.List;
import java.util.Optional;

public interface QuestionService {

    QuestionResponse saveQuestion(QuestionRequest questionRequest, Long userId);

    Question updateQuestion(Question question, List<String> tagNames);

    Optional<Question> getQuestionById(Long id);

    void deleteQuestion(Long id);

    void markAsSolved(Question question);

    List<QuestionResponse> getActiveQuestions();

    List<QuestionResponse> searchSimilarQuestions(String text);
}
