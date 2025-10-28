package com.example.project.controller;

import com.example.project.entity.req.AnswerRequest;
import com.example.project.entity.res.AnswerResponse;
import com.example.project.entity.CustomUserDetails;
import com.example.project.entity.Answer;
import com.example.project.entity.Question;
import com.example.project.service.AnswerService;
import com.example.project.util.AdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(answerService.getAnswersByUser(userId));
    }

    @PutMapping("/{answerId}/accept")
    public ResponseEntity<Void> acceptAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Answer answer = answerService.getAnswerById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        Question question = answer.getQuestion();
        if (question == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Sadece soru sahibi veya admin bu işlemi yapabilir
        if (!AdminUtil.canModifyContent(question.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        answerService.acceptAnswer(answerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{answerId}/accept")
    public ResponseEntity<Void> deleteAcceptAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Answer answer = answerService.getAnswerById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        Question question = answer.getQuestion();
        if (question == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Sadece soru sahibi veya admin bu işlemi yapabilir
        if (!AdminUtil.canModifyContent(question.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        answerService.deleteAcceptAnswer(answerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Answer answer = answerService.getAnswerById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        // Admin ise veya cevap sahibi ise silme işlemini yap
        if (!AdminUtil.canDeleteContent(answer.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        answerService.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }
}
