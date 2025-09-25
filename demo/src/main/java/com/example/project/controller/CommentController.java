package com.example.project.controller;

import com.example.project.entity.req.CommentRequest;
import com.example.project.entity.res.CommentResponse;
import com.example.project.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ✅ Yorum oluştur
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(request));
    }

    // ✅ Belirli bir cevaba ait yorumları getir
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByAnswer(@PathVariable Long answerId) {
        return ResponseEntity.ok(commentService.getCommentsByAnswer(answerId));
    }

    // ✅ Belirli bir kullanıcıya ait yorumları getir
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUser(userId));
    }

    // ✅ Yorum güncelle
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    // ✅ Yorum sil (soft delete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
