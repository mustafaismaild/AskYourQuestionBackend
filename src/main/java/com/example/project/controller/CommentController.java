package com.example.project.controller;

import com.example.project.entity.req.AnswerCommentRequest;
import com.example.project.entity.req.CommentRequest;
import com.example.project.entity.res.CommentResponse;
import com.example.project.entity.CustomUserDetails;
import com.example.project.entity.Comment;
import com.example.project.service.CommentService;
import com.example.project.util.AdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ✅ Cevaba yorum yap (questionId otomatik çekilir)
    @PostMapping("/answer")
    public ResponseEntity<CommentResponse> createAnswerComment(
            @RequestBody AnswerCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentService.createAnswerComment(request, userDetails.getId()));
    }


    // ✅ Eski endpoint - backward compatibility için
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentService.createComment(request, userDetails.getId()));
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
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Comment comment = commentService.getCommentById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        // Sadece yorum sahibi veya admin bu işlemi yapabilir
        if (!AdminUtil.canModifyContent(comment.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    // ✅ Yorum sil (soft delete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Comment comment = commentService.getCommentById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        // Admin ise veya yorum sahibi ise silme işlemini yap
        if (!AdminUtil.canDeleteContent(comment.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
