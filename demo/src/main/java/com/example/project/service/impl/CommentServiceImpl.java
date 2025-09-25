package com.example.project.service.impl;

import com.example.project.entity.*;
import com.example.project.entity.req.CommentRequest;
import com.example.project.entity.res.CommentResponse;
import com.example.project.repository.AnswerRepository;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.QuestionRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    @Override
    public CommentResponse createComment(CommentRequest request) {
        // Token’dan userId al
        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userIdFromToken = userDetails.getId();

        User user = userRepository.findById(userIdFromToken)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer answer = answerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setAnswer(answer);
        comment.setQuestion(question);
        comment.setStatus(true);
        comment.setCreatedAt(LocalDateTime.now());

        return mapToResponse(commentRepository.save(comment));
    }


    @Override
    public List<CommentResponse> getCommentsByAnswer(Long answerId) {
        return commentRepository.findByAnswerIdAndStatus(answerId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentResponse> getCommentsByUser(Long userId) {
        return commentRepository.findByUserIdAndStatus(userId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // ❌ DB'den silme yerine soft delete
        comment.setStatus(false);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUser().getId());
        response.setAnswerId(comment.getAnswer().getId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setStatus(comment.isStatus());

        // ✅ Oy sayısını response’a ekle
        if (comment.getVotes() != null) {
            response.setVoteCount(comment.getVotes().size());
        } else {
            response.setVoteCount(0);
        }

        return response;
    }

}
