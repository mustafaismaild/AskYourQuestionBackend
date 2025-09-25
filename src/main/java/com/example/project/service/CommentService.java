package com.example.project.service;

import com.example.project.entity.req.CommentRequest;
import com.example.project.entity.res.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(CommentRequest request);
    List<CommentResponse> getCommentsByAnswer(Long answerId);
    List<CommentResponse> getCommentsByUser(Long userId);
    CommentResponse updateComment(Long commentId, CommentRequest request);
    void deleteComment(Long commentId);
}
