package com.example.project.service;

import com.example.project.entity.req.SavedQuestionRequest;
import com.example.project.entity.res.SavedQuestionResponse;

import java.util.List;

public interface SavedQuestionService {
    SavedQuestionResponse saveQuestion(Long userId, SavedQuestionRequest request);
    List<SavedQuestionResponse> getSavedQuestions(Long userId);
    void deleteSavedQuestion(Long userId, Long savedId);
}
