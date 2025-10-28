package com.example.project.service;

import com.example.project.entity.Question;
import com.example.project.entity.req.QuestionRequest;
import com.example.project.entity.res.QuestionResponse;

import java.util.List;
import java.util.Optional;

public interface QuestionService {

    QuestionResponse saveQuestion(QuestionRequest questionRequest, Long userId, String userName);
    QuestionResponse saveQuestionWithImage(QuestionRequest questionRequest, Long userId, String userName);

    Question updateQuestion(Question question, List<String> tagNames);

    Optional<Question> getQuestionById(Long id);

    void deleteQuestion(Long id);

    void markAsSolved(Long questionId, String username);

    void unsolveQuestion(Long questionId, String username);

    List<QuestionResponse> getActiveQuestions();

    List<QuestionResponse> searchSimilarQuestions(String text);

    List<QuestionResponse> getQuestionsByTagName(String tagName);

    List<QuestionResponse> getQuestionsByTagId(Long tagId);

    List<QuestionResponse> getQuestionsByUser(Long userId);

    // ✅ Yeni eklenen metodlar
    List<QuestionResponse> getMostViewedQuestions();
    List<QuestionResponse> getMostVisitedQuestions();
    List<QuestionResponse> getLatestQuestions();
    List<QuestionResponse> getUnansweredQuestions();
    List<QuestionResponse> getRecentlySolvedQuestions();
    List<QuestionResponse> fullTextSearch(String searchTerm);
    void incrementViewCount(Long questionId);
}
