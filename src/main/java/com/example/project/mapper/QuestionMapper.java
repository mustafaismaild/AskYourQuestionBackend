package com.example.project.mapper;

import com.example.project.entity.Question;
import com.example.project.entity.Tag;
import com.example.project.entity.res.QuestionResponse;

import java.util.stream.Collectors;

public class QuestionMapper {

    public static QuestionResponse toResponse(Question question) {
        if (question == null) return null;

        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setTitle(question.getTitle());
        response.setContent(question.getContent());
        response.setStatus(question.isStatus());
        response.setSolved(question.isSolved());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());
        response.setUserId(question.getUser() != null ? question.getUser().getId() : null);

        if (question.getTags() != null && !question.getTags().isEmpty()) {
            response.setTags(
                    question.getTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.toList())
            );
        }

        if (question.getAnswers() != null && !question.getAnswers().isEmpty()) {
            response.setAnswers(
                    question.getAnswers().stream()
                            .map(answer -> {
                                var ansResp = new com.example.project.entity.res.AnswerResponse();
                                ansResp.setId(answer.getId());
                                ansResp.setContent(answer.getContent());
                                ansResp.setUserId(answer.getUser() != null ? answer.getUser().getId() : null);
                                ansResp.setCreatedAt(answer.getCreatedAt());
                                ansResp.setUpdatedAt(answer.getUpdatedAt());
                                // question bilgisini eklemiyoruz → döngüyü önler
                                return ansResp;
                            })
                            .collect(Collectors.toList())
            );
        }

        return response;
    }
}
