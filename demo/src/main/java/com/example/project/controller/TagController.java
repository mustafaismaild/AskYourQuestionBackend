package com.example.project.controller;

import com.example.project.entity.req.TagRequest;
import com.example.project.entity.res.TagResponse;
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
}
