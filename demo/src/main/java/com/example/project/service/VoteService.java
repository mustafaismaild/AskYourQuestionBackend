package com.example.project.service;

import com.example.project.entity.res.VoteResponse;
import java.util.List;

public interface VoteService {
    VoteResponse voteQuestion(Long questionId, Long userId, int value);
    VoteResponse voteAnswer(Long answerId, Long userId, int value);
    VoteResponse voteComment(Long commentId, Long userId, int value);

    List<VoteResponse> getVotesByQuestion(Long userId, Long questionId);
    List<VoteResponse> getVotesByAnswer(Long userId, Long answerId);
    List<VoteResponse> getVotesByComment(Long userId, Long commentId); // <- burası olmalı

    void deleteVote(Long voteId);
}
