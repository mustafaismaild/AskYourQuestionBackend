package com.example.project.service;

import com.example.project.entity.res.VoteResponse;
import java.util.List;

public interface VoteService {
    VoteResponse voteQuestion(Long questionId, Long userId, int value);
    VoteResponse voteAnswer(Long answerId, Long userId, int value);
    VoteResponse voteComment(Long commentId, Long userId, int value);

    List<VoteResponse> getVotesByQuestion(Long userId, Long questionId);
    List<VoteResponse> getVotesByAnswer(Long userId, Long answerId);
    List<VoteResponse> getVotesByComment(Long userId, Long commentId);

    int getQuestionTotalScore(Long questionId);
    int getAnswerTotalScore(Long answerId);
    int getCommentTotalScore(Long commentId);

    void deleteVote(Long voteId);
    void removeQuestionVote(Long questionId, Long userId);
    void removeAnswerVote(Long answerId, Long userId);
    void removeCommentVote(Long commentId, Long userId);

    int getUserQuestionVote(Long questionId, Long userId);
    int getUserAnswerVote(Long answerId, Long userId);
    int getUserCommentVote(Long commentId, Long userId);
}
