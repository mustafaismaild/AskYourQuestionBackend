package com.example.project.controller;

import com.example.project.entity.res.VoteResponse;
import com.example.project.entity.CustomUserDetails;
import com.example.project.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/question/{questionId}/up")
    public ResponseEntity<VoteResponse> upvoteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.voteQuestion(questionId, userDetails.getId(), +1));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/question/{questionId}/down")
    public ResponseEntity<VoteResponse> downvoteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.voteQuestion(questionId, userDetails.getId(), -1));
    }

    // ✅ Cevaba upvote
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/{answerId}/up")
    public ResponseEntity<VoteResponse> upvoteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.voteAnswer(answerId, userDetails.getId(), +1));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/{answerId}/down")
    public ResponseEntity<VoteResponse> downvoteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.voteAnswer(answerId, userDetails.getId(), -1));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comment/{commentId}/up")
    public ResponseEntity<VoteResponse> upvoteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.voteComment(commentId, userDetails.getId(), +1));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comment/{commentId}/down")
    public ResponseEntity<VoteResponse> downvoteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.voteComment(commentId, userDetails.getId(), -1));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{voteId}")
    public ResponseEntity<Void> deleteVote(
            @PathVariable Long voteId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        voteService.deleteVote(voteId);
        return ResponseEntity.noContent().build();
    }

    private void validateUser(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Bu işlemi yapabilmek için giriş yapmalısınız.");
        }
    }
}
