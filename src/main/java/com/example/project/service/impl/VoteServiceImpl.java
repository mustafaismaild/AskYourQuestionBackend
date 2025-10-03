package com.example.project.service.impl;

import com.example.project.entity.*;
import com.example.project.entity.res.VoteResponse;
import com.example.project.enums.VoteType;
import com.example.project.mapper.VoteMapper;
import com.example.project.repository.*;
import com.example.project.service.VoteService;
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

        int totalScore = voteRepository.findAllByQuestionIdAndStatus(questionId, true).stream()
                .mapToInt(v -> v.getType() == VoteType.UPVOTE ? 1 : -1)
                .sum();

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

        int totalScore = voteRepository.findAllByAnswerIdAndStatus(answerId, true).stream()
                .mapToInt(v -> v.getType() == VoteType.UPVOTE ? 1 : -1)
                .sum();

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

        int totalScore = voteRepository.findAllByCommentIdAndStatus(commentId, true).stream()
                .mapToInt(v -> v.getType() == VoteType.UPVOTE ? 1 : -1)
                .sum();

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
    public void deleteVote(Long voteId) {
        voteRepository.findById(voteId).ifPresent(vote -> {
            vote.setStatus(false);
            vote.setUpdatedAt(LocalDateTime.now());
            voteRepository.save(vote);
        });
    }
}
