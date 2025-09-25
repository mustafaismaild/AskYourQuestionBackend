package com.example.project.controller;

import com.example.project.entity.req.AnswerRequest;
import com.example.project.entity.res.AnswerResponse;
import com.example.project.entity.CustomUserDetails;
import com.example.project.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;


    @PostMapping
    public ResponseEntity<AnswerResponse> createAnswer(
            @RequestBody AnswerRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AnswerResponse answer = answerService.createAnswer(request, userDetails.getId());
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.getAnswersByQuestion(questionId));
    }

    @PutMapping("/{answerId}/accept")
    public ResponseEntity<Void> acceptAnswer(@PathVariable Long answerId) {
        answerService.acceptAnswer(answerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long answerId) {
        answerService.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }
}
