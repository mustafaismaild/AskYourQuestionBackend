package com.example.project.service;

import com.example.project.entity.Question;
import com.example.project.entity.req.AnswerRequest;
import com.example.project.entity.res.AnswerResponse;

import java.util.List;

public interface AnswerService {

    AnswerResponse createAnswer(AnswerRequest request, Long userId);

    List<AnswerResponse> getAnswersByQuestion(Long questionId);

    void acceptAnswer(Long id);

    void deleteAnswer(Long id);
}
