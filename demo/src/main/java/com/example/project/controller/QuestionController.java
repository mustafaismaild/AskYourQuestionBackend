package com.example.project.controller;

import com.example.project.mapper.QuestionMapper;
import com.example.project.repository.UserRepository;
import com.example.project.entity.Question;
import com.example.project.entity.req.QuestionRequest;
import com.example.project.entity.res.QuestionResponse;
import com.example.project.entity.CustomUserDetails;
import com.example.project.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final UserRepository userRepository;

    public QuestionController(QuestionService questionService, UserRepository userRepository) {
        this.questionService = questionService;
        this.userRepository = userRepository;
    }
    @PostMapping("/saveQuestion")
    public ResponseEntity<QuestionResponse> saveQuestion(
            @RequestBody QuestionRequest questionRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        QuestionResponse response = questionService.saveQuestion(questionRequest, userDetails.getId());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/getAll")
    public ResponseEntity<List<QuestionResponse>> getActiveQuestions() {
        return ResponseEntity.ok(
                questionService.getActiveQuestions()
                        .stream()
                        .map(QuestionMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id)
                .map(q -> ResponseEntity.ok(QuestionMapper.toResponse(q)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id, @RequestBody QuestionRequest request) {
        return questionService.getQuestionById(id)
                .map(existing -> {
                    existing.setTitle(request.getTitle());
                    existing.setContent(request.getContent());
                    Question updated = questionService.updateQuestion(existing, request.getTags());
                    return ResponseEntity.ok(QuestionMapper.toResponse(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuestionResponse>> searchSimilarQuestions(@RequestParam String text) {
        return ResponseEntity.ok(questionService.searchSimilarQuestions(text));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/solve")
    public ResponseEntity<Void> markAsSolved(@PathVariable Long id) {
        Optional<Question> questionOpt = questionService.getQuestionById(id);

        if (questionOpt.isPresent()) {
            questionService.markAsSolved(questionOpt.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
