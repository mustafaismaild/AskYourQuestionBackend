package com.example.project.controller;

import com.example.project.entity.req.TagRequest;
import com.example.project.entity.res.QuestionResponse;
import com.example.project.entity.res.TagResponse;
import com.example.project.service.QuestionService;
import com.example.project.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(@PathVariable Long id,
                                                 @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByTagId(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionsByTagId(id));
    }

    @GetMapping("/name/{tagName}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByTagName(@PathVariable String tagName) {
        return ResponseEntity.ok(questionService.getQuestionsByTagName(tagName));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<TagResponse>> getPopularTags() {
        return ResponseEntity.ok(tagService.getPopularTags());
    }

    @GetMapping("/popular/{limit}")
    public ResponseEntity<List<TagResponse>> getPopularTags(@PathVariable int limit) {
        return ResponseEntity.ok(tagService.getPopularTags(limit));
    }
}
