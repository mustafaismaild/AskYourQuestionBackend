package com.example.project.service;

import com.example.project.entity.Comment;
import com.example.project.entity.req.AnswerCommentRequest;
import com.example.project.entity.req.CommentRequest;
import com.example.project.entity.res.CommentResponse;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    // ✅ Cevaba yorum yapma (questionId otomatik çekilir)
    CommentResponse createAnswerComment(AnswerCommentRequest request, Long userId);
    
    // ✅ Eski metod - backward compatibility için
    CommentResponse createComment(CommentRequest request, Long userId);
    
    List<CommentResponse> getCommentsByAnswer(Long answerId);
    List<CommentResponse> getCommentsByUser(Long userId);
    CommentResponse updateComment(Long commentId, CommentRequest request);
    void deleteComment(Long commentId);
    Optional<Comment> getCommentById(Long id);
}
