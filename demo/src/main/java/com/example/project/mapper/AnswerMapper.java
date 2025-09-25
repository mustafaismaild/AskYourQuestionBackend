package com.example.project.mapper;

import com.example.project.entity.Answer;
import com.example.project.entity.res.AnswerResponse;

public class AnswerMapper {

    public static AnswerResponse toResponse(Answer answer) {
        AnswerResponse response = new AnswerResponse();
        response.setId(answer.getId());
        response.setContent(answer.getContent());
        response.setCreatedAt(answer.getCreatedAt());
        response.setUpdatedAt(answer.getUpdatedAt());
        response.setStatus(answer.isStatus());

        if (answer.getUser() != null) {
            response.setUserId(answer.getUser().getId());
            response.setUsername(answer.getUser().getUsername());
        }

        if (answer.getQuestion() != null) {
            response.setQuestionId(answer.getQuestion().getId());
        }

        return response;
    }
}
