package com.example.project.entity.res;

import java.time.LocalDateTime;


public record SavedQuestionResponse(
        Long id,
        Long userId,
        Long questionId,
        String questionTitle,
        LocalDateTime createdAt
) {

}