package com.example.project.mapper;

import com.example.project.entity.Question;
import com.example.project.entity.Tag;
import com.example.project.entity.res.QuestionResponse;
import com.example.project.entity.res.AnswerResponse;

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
        response.setUserName(question.getUser() != null ? question.getUser().getUsername() : null);
        
        // ✅ Vote count ve view count ekle
        response.setVoteCount(question.getVoteCount() != null ? question.getVoteCount() : 0);
        response.setViewCount(question.getViewCount() != null ? question.getViewCount() : 0);

        if (question.getTags() != null && !question.getTags().isEmpty()) {
            response.setTags(
                    question.getTags().stream()
                            .map(Tag::getName)
                            .toList()
            );
        }

        if (question.getAnswers() != null && !question.getAnswers().isEmpty()) {
            response.setAnswers(
                    question.getAnswers().stream()
                            .map(answer -> {
                                AnswerResponse ansResp = new AnswerResponse();
                                ansResp.setId(answer.getId());
                                ansResp.setContent(answer.getContent());
                                ansResp.setUserId(answer.getUser() != null ? answer.getUser().getId() : null);
                                ansResp.setUsername(answer.getUser() != null ? answer.getUser().getUsername() : null);
                                ansResp.setCreatedAt(answer.getCreatedAt());
                                ansResp.setUpdatedAt(answer.getUpdatedAt());
                                ansResp.setVoteCount(answer.getVoteCount() != null ? answer.getVoteCount() : 0);
                                // Question bilgisi eklemiyoruz → döngüyü önler
                                return ansResp;
                            })
                            .collect(Collectors.toList())
            );
        }

        return response;
    }
}
