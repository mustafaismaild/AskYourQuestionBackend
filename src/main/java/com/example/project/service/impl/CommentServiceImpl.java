package com.example.project.service.impl;

import com.example.project.entity.*;
import com.example.project.entity.req.AnswerCommentRequest;
import com.example.project.entity.req.CommentRequest;
import com.example.project.entity.res.CommentResponse;
import com.example.project.repository.AnswerRepository;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.CommentService;
import com.example.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final NotificationService notificationService;

    // ✅ Yeni metod - Cevaba yorum yapma (questionId otomatik çekilir)
    @Override
    public CommentResponse createAnswerComment(AnswerCommentRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer answer = answerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        // ✅ Answer'dan Question'ı otomatik olarak çek
        Question question = answer.getQuestion();

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setAnswer(answer);
        comment.setQuestion(question);
        comment.setStatus(true);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        
        // Bildirim gönder - sadece kendi cevabına yorum yapmıyorsa
        if (!answer.getUser().getId().equals(userId)) {
            notificationService.notifyNewComment(answer.getUser(), answer.getId(), "ANSWER");
        }

        // Mention tespiti yap
        detectAndNotifyMentions(comment.getContent(), user, savedComment.getId(), "COMMENT");

        return mapToResponse(savedComment);
    }


    // ✅ Eski metod - backward compatibility için
    @Override
    public CommentResponse createComment(CommentRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer answer = answerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        // ✅ Answer'dan Question'ı otomatik olarak çek
        Question question = answer.getQuestion();

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setAnswer(answer);
        comment.setQuestion(question);
        comment.setStatus(true);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        
        // Bildirim gönder - sadece kendi cevabına yorum yapmıyorsa
        if (!answer.getUser().getId().equals(userId)) {
            notificationService.notifyNewComment(answer.getUser(), answer.getId(), "ANSWER");
        }

        // Mention tespiti yap
        detectAndNotifyMentions(comment.getContent(), user, savedComment.getId(), "COMMENT");

        return mapToResponse(savedComment);
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

    @Override
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    private void detectAndNotifyMentions(String content, User author, Long entityId, String entityType) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        Matcher matcher = mentionPattern.matcher(content);
        
        while (matcher.find()) {
            String mentionedUsername = matcher.group(1);
            userRepository.findByUsername(mentionedUsername).ifPresent(mentionedUser -> {
                // Kendi kendini bahsetmiyorsa bildirim gönder
                if (!mentionedUser.getId().equals(author.getId())) {
                    notificationService.notifyMention(mentionedUser, author, entityId, entityType);
                }
            });
        }
    }


    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUser().getId());
        response.setUsername(comment.getUser().getUsername());
        response.setAvatarUrl(comment.getUser().getAvatarUrl());
        
        // ✅ Sadece cevaba yorum yapıyoruz, bu yüzden answer her zaman var
        response.setAnswerId(comment.getAnswer().getId());
        
        // ✅ Question her zaman var (answer'dan geliyor)
        response.setQuestionId(comment.getQuestion().getId());
        
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setStatus(comment.isStatus());

        // ✅ Oy sayısını response'a ekle (artık veritabanından geliyor)
        response.setVoteCount(comment.getVoteCount() != null ? comment.getVoteCount() : 0);

        return response;
    }

}
