package com.example.project.service;

import com.example.project.entity.Answer;
import com.example.project.entity.Question;
import com.example.project.entity.req.AnswerRequest;
import com.example.project.entity.res.AnswerResponse;

import java.util.List;
import java.util.Optional;

public interface AnswerService {

    AnswerResponse createAnswer(AnswerRequest request, Long userId);

    List<AnswerResponse> getAnswersByQuestion(Long questionId);

    List<AnswerResponse> getAnswersByUser(Long userId);

    void acceptAnswer(Long id);

    void deleteAcceptAnswer(Long id);

    void deleteAnswer(Long id);

    Optional<Answer> getAnswerById(Long id);
}
