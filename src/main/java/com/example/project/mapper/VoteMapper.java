package com.example.project.mapper;

import com.example.project.entity.Answer;
import com.example.project.entity.Vote;
import com.example.project.enums.VoteType;
import com.example.project.entity.res.VoteResponse;

public class VoteMapper {

    public static VoteResponse toResponse(Vote vote, int totalScore, boolean userVoted) {
        VoteResponse response = new VoteResponse();

        response.setVoteId(vote.getId());
        response.setUserId(vote.getUser() != null ? vote.getUser().getId() : null);
        response.setCommentId(vote.getComment() != null ? vote.getComment().getId() : null);

        response.setAnswerId(null);
        response.setQuestionId(null);

        if (vote.getComment() != null) {
            if (vote.getComment().getAnswer() != null) {
                Answer ans = vote.getComment().getAnswer();
                response.setAnswerId(ans.getId());
                if (ans.getQuestion() != null) {
                    response.setQuestionId(ans.getQuestion().getId());
                }
            } else if (vote.getComment().getQuestion() != null) {
                response.setQuestionId(vote.getComment().getQuestion().getId());
            }
        }
        else if (vote.getAnswer() != null) {
            response.setAnswerId(vote.getAnswer().getId());
            if (vote.getAnswer().getQuestion() != null) {
                response.setQuestionId(vote.getAnswer().getQuestion().getId());
            }
        }
        else if (vote.getQuestion() != null) {
            response.setQuestionId(vote.getQuestion().getId());
        }

        if (vote.getValue() != null) {
            response.setValue(vote.getValue());
        } else if (vote.getType() != null) {
            response.setValue(vote.getType() == VoteType.UPVOTE ? 1 : -1);
        } else {
            response.setValue(null);
        }

        response.setTotalScore(totalScore);
        response.setUserVoted(userVoted);

        return response;
    }
}
