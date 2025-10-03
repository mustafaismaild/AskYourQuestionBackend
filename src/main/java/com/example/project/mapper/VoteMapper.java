package com.example.project.mapper;

import com.example.project.entity.Answer;
import com.example.project.entity.Vote;
import com.example.project.enums.VoteType;
import com.example.project.entity.res.VoteResponse;

public class VoteMapper {

    public static VoteResponse toResponse(Vote vote, int totalScore, Integer userVote) {
        if (vote == null) {
            return null;
        }

        VoteResponse response = new VoteResponse();

        // Vote ID
        response.setVoteId(vote.getId());

        if (vote.getUser() != null) {
            response.setUserId(vote.getUser().getId());
        }

        if (vote.getComment() != null) {
            response.setCommentId(vote.getComment().getId());
        }

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

        if (userVote != null) {
            response.setUserVote(userVote);
        } else if (vote.getValue() != null) {
            response.setUserVote(vote.getValue());
        } else if (vote.getType() != null) {
            response.setUserVote(vote.getType() == VoteType.UPVOTE ? 1 : -1);
        } else {
            response.setUserVote(0); // default
        }

        response.setTotalScore(totalScore);

        return response;
    }
}
