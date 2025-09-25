package com.example.project.controller;

import com.example.project.entity.req.SavedQuestionRequest;
import com.example.project.entity.res.SavedQuestionResponse;
import com.example.project.service.SavedQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved")
public class SavedQuestionController {

    private final SavedQuestionService savedQuestionService;

    public SavedQuestionController(SavedQuestionService savedQuestionService) {
        this.savedQuestionService = savedQuestionService;
    }

    @PostMapping
    public ResponseEntity<SavedQuestionResponse> saveQuestion(
            @AuthenticationPrincipal com.example.project.entity.CustomUserDetails userDetails,
            @RequestBody SavedQuestionRequest request
    ) {
        return ResponseEntity.ok(savedQuestionService.saveQuestion(userDetails.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<SavedQuestionResponse>> getSavedQuestions(
            @AuthenticationPrincipal com.example.project.entity.CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(savedQuestionService.getSavedQuestions(userDetails.getId()));
    }

    @DeleteMapping("/{savedId}")
    public ResponseEntity<Void> deleteSavedQuestion(
            @AuthenticationPrincipal com.example.project.entity.CustomUserDetails userDetails,
            @PathVariable Long savedId
    ) {
        savedQuestionService.deleteSavedQuestion(userDetails.getId(), savedId);
        return ResponseEntity.noContent().build();
    }
}
