package com.example.project.service.impl;

import com.example.project.entity.*;
import com.example.project.entity.res.VoteResponse;
import com.example.project.enums.VoteType;
import com.example.project.mapper.VoteMapper;
import com.example.project.repository.*;
import com.example.project.service.VoteService;
import com.example.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Override
    public VoteResponse voteQuestion(Long questionId, Long userId, int value) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Vote vote = voteRepository.findByUserIdAndQuestionIdAndStatus(userId, questionId, true)
                .orElse(new Vote());

        vote.setQuestion(question);
        vote.setUser(user);

        VoteType voteType = value == 1 ? VoteType.UPVOTE : VoteType.DOWNVOTE;
        vote.setType(voteType);
        vote.setValue(value);
        vote.setStatus(true);

        if (vote.getCreatedAt() == null) {
            vote.setCreatedAt(LocalDateTime.now());
        }
        vote.setUpdatedAt(LocalDateTime.now());

        voteRepository.save(vote);

        // ✅ Question'ın vote count'unu güncelle
        updateQuestionVoteCount(questionId);

        // Bildirim gönder - sadece kendi sorusuna oy vermiyorsa
        if (!question.getUser().getId().equals(userId)) {
            notificationService.notifyVoteReceived(question.getUser(), questionId, "QUESTION");
        }

        // Güncel toplam score'u veritabanından al
        int totalScore = voteRepository.sumByQuestionId(questionId);

        return VoteMapper.toResponse(vote, totalScore, value);
    }

    @Override
    public VoteResponse voteAnswer(Long answerId, Long userId, int value) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        Vote vote = voteRepository.findByUserIdAndAnswerIdAndStatus(userId, answerId, true)
                .orElse(new Vote());

        vote.setUser(user);
        vote.setAnswer(answer);
        vote.setType(value == 1 ? VoteType.UPVOTE : VoteType.DOWNVOTE);
        vote.setValue(value);
        vote.setStatus(true);

        if (vote.getCreatedAt() == null) {
            vote.setCreatedAt(LocalDateTime.now());
        }
        vote.setUpdatedAt(LocalDateTime.now());

        voteRepository.save(vote);

        // ✅ Answer'ın vote count'unu güncelle
        updateAnswerVoteCount(answerId);

        // Bildirim gönder - sadece kendi cevabına oy vermiyorsa
        if (!answer.getUser().getId().equals(userId)) {
            notificationService.notifyVoteReceived(answer.getUser(), answerId, "ANSWER");
        }

        // Güncel toplam score'u veritabanından al
        int totalScore = voteRepository.sumByAnswerId(answerId);

        return VoteMapper.toResponse(vote, totalScore, value);
    }

    @Override
    public VoteResponse voteComment(Long commentId, Long userId, int value) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Vote vote = voteRepository.findByUserIdAndCommentIdAndStatus(userId, commentId, true)
                .orElse(new Vote());

        vote.setUser(user);
        vote.setComment(comment);
        vote.setType(value == 1 ? VoteType.UPVOTE : VoteType.DOWNVOTE);
        vote.setValue(value);
        vote.setStatus(true);

        if (vote.getCreatedAt() == null) {
            vote.setCreatedAt(LocalDateTime.now());
        }
        vote.setUpdatedAt(LocalDateTime.now());

        voteRepository.save(vote);

        // ✅ Comment'in vote count'unu güncelle
        updateCommentVoteCount(commentId);

        // Bildirim gönder - sadece kendi yorumuna oy vermiyorsa
        if (!comment.getUser().getId().equals(userId)) {
            notificationService.notifyVoteReceived(comment.getUser(), commentId, "COMMENT");
        }

        // Güncel toplam score'u veritabanından al
        int totalScore = voteRepository.sumByCommentId(commentId);

        return VoteMapper.toResponse(vote, totalScore, value);
    }

    @Override
    public List<VoteResponse> getVotesByQuestion(Long userId, Long questionId) {
        List<Vote> votes = voteRepository.findAllByQuestionIdAndStatus(questionId, true);

        int totalScore = votes.stream()
                .mapToInt(v -> v.getType() == VoteType.UPVOTE ? 1 : -1)
                .sum();

        // Kullanıcının oyu
        Integer userVote = votes.stream()
                .filter(v -> v.getUser().getId().equals(userId))
                .map(Vote::getValue)
                .findFirst()
                .orElse(0);

        return votes.stream()
                .map(v -> VoteMapper.toResponse(v, totalScore, userVote))
                .toList();
    }

    @Override
    public List<VoteResponse> getVotesByAnswer(Long userId, Long answerId) {
        List<Vote> votes = voteRepository.findAllByAnswerIdAndStatus(answerId, true);

        int totalScore = votes.stream()
                .mapToInt(v -> v.getType() == VoteType.UPVOTE ? 1 : -1)
                .sum();

        Integer userVote = votes.stream()
                .filter(v -> v.getUser().getId().equals(userId))
                .map(Vote::getValue)
                .findFirst()
                .orElse(0);

        return votes.stream()
                .map(v -> VoteMapper.toResponse(v, totalScore, userVote))
                .toList();
    }

    @Override
    public List<VoteResponse> getVotesByComment(Long userId, Long commentId) {
        List<Vote> votes = voteRepository.findAllByCommentIdAndStatus(commentId, true);

        int totalScore = votes.stream()
                .mapToInt(v -> v.getType() == VoteType.UPVOTE ? 1 : -1)
                .sum();

        Integer userVote = votes.stream()
                .filter(v -> v.getUser().getId().equals(userId))
                .map(Vote::getValue)
                .findFirst()
                .orElse(0);

        return votes.stream()
                .map(v -> VoteMapper.toResponse(v, totalScore, userVote))
                .toList();
    }

    @Override
    public int getQuestionTotalScore(Long questionId) {
        return voteRepository.sumByQuestionId(questionId);
    }

    @Override
    public int getAnswerTotalScore(Long answerId) {
        return voteRepository.sumByAnswerId(answerId);
    }

    @Override
    public int getCommentTotalScore(Long commentId) {
        return voteRepository.sumByCommentId(commentId);
    }

    @Override
    public void deleteVote(Long voteId) {
        voteRepository.findById(voteId).ifPresent(vote -> {
            vote.setStatus(false);
            vote.setUpdatedAt(LocalDateTime.now());
            voteRepository.save(vote);
        });
    }

    @Override
    public void removeQuestionVote(Long questionId, Long userId) {
        voteRepository.findByUserIdAndQuestionIdAndStatus(userId, questionId, true)
                .ifPresent(vote -> {
                    vote.setStatus(false);
                    vote.setUpdatedAt(LocalDateTime.now());
                    voteRepository.save(vote);
                    
                    // ✅ Question'ın vote count'unu güncelle
                    updateQuestionVoteCount(questionId);
                });
    }

    @Override
    public void removeAnswerVote(Long answerId, Long userId) {
        voteRepository.findByUserIdAndAnswerIdAndStatus(userId, answerId, true)
                .ifPresent(vote -> {
                    vote.setStatus(false);
                    vote.setUpdatedAt(LocalDateTime.now());
                    voteRepository.save(vote);
                    
                    // ✅ Answer'ın vote count'unu güncelle
                    updateAnswerVoteCount(answerId);
                });
    }

    @Override
    public void removeCommentVote(Long commentId, Long userId) {
        voteRepository.findByUserIdAndCommentIdAndStatus(userId, commentId, true)
                .ifPresent(vote -> {
                    vote.setStatus(false);
                    vote.setUpdatedAt(LocalDateTime.now());
                    voteRepository.save(vote);
                    
                    // ✅ Comment'in vote count'unu güncelle
                    updateCommentVoteCount(commentId);
                });
    }

    @Override
    public int getUserQuestionVote(Long questionId, Long userId) {
        return voteRepository.findByUserIdAndQuestionIdAndStatus(userId, questionId, true)
                .map(Vote::getValue)
                .orElse(0);
    }

    @Override
    public int getUserAnswerVote(Long answerId, Long userId) {
        return voteRepository.findByUserIdAndAnswerIdAndStatus(userId, answerId, true)
                .map(Vote::getValue)
                .orElse(0);
    }

    @Override
    public int getUserCommentVote(Long commentId, Long userId) {
        return voteRepository.findByUserIdAndCommentIdAndStatus(userId, commentId, true)
                .map(Vote::getValue)
                .orElse(0);
    }

    // ✅ Vote count güncelleme metodları
    private void updateQuestionVoteCount(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        int voteCount = voteRepository.countByQuestionIdAndStatus(questionId, true);
        int upvoteCount = voteRepository.countUpvotesByQuestionIdAndStatus(questionId, true);
        int downvoteCount = voteRepository.countDownvotesByQuestionIdAndStatus(questionId, true);
        
        question.setVoteCount(voteCount);
        question.setUpvoteCount(upvoteCount);
        question.setDownvoteCount(downvoteCount);
        questionRepository.save(question);
    }

    private void updateAnswerVoteCount(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        
        int voteCount = voteRepository.countByAnswerIdAndStatus(answerId, true);
        int upvoteCount = voteRepository.countUpvotesByAnswerIdAndStatus(answerId, true);
        int downvoteCount = voteRepository.countDownvotesByAnswerIdAndStatus(answerId, true);
        
        answer.setVoteCount(voteCount);
        answer.setUpvoteCount(upvoteCount);
        answer.setDownvoteCount(downvoteCount);
        answerRepository.save(answer);
    }

    private void updateCommentVoteCount(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        int voteCount = voteRepository.countByCommentIdAndStatus(commentId, true);
        int upvoteCount = voteRepository.countUpvotesByCommentIdAndStatus(commentId, true);
        int downvoteCount = voteRepository.countDownvotesByCommentIdAndStatus(commentId, true);
        
        comment.setVoteCount(voteCount);
        comment.setUpvoteCount(upvoteCount);
        comment.setDownvoteCount(downvoteCount);
        commentRepository.save(comment);
    }
}
