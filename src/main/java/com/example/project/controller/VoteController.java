package com.example.project.controller;

import com.example.project.entity.res.VoteResponse;
import com.example.project.entity.CustomUserDetails;
import com.example.project.service.VoteService;
import com.example.project.util.ValidationUtil;
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

    // ✅ Kullanıcının soruya verdiği oyu kaldır
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<Void> removeQuestionVote(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        voteService.removeQuestionVote(questionId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // ✅ Kullanıcının cevaba verdiği oyu kaldır
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/answer/{answerId}")
    public ResponseEntity<Void> removeAnswerVote(
            @PathVariable Long answerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        voteService.removeAnswerVote(answerId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // ✅ Kullanıcının yoruma verdiği oyu kaldır
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> removeCommentVote(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        voteService.removeCommentVote(commentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/question/{questionId}/score")
    public ResponseEntity<Integer> getQuestionScore(@PathVariable Long questionId) {
        return ResponseEntity.ok(voteService.getQuestionTotalScore(questionId));
    }

    @GetMapping("/answer/{answerId}/score")
    public ResponseEntity<Integer> getAnswerScore(@PathVariable Long answerId) {
        return ResponseEntity.ok(voteService.getAnswerTotalScore(answerId));
    }

    @GetMapping("/comment/{commentId}/score")
    public ResponseEntity<Integer> getCommentScore(@PathVariable Long commentId) {
        return ResponseEntity.ok(voteService.getCommentTotalScore(commentId));
    }

    // ✅ Kullanıcının soruya verdiği oyu kontrol et
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/question/{questionId}/my-vote")
    public ResponseEntity<Integer> getMyQuestionVote(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.getUserQuestionVote(questionId, userDetails.getId()));
    }

    // ✅ Kullanıcının cevaba verdiği oyu kontrol et
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/answer/{answerId}/my-vote")
    public ResponseEntity<Integer> getMyAnswerVote(
            @PathVariable Long answerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.getUserAnswerVote(answerId, userDetails.getId()));
    }

    // ✅ Kullanıcının yoruma verdiği oyu kontrol et
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/comment/{commentId}/my-vote")
    public ResponseEntity<Integer> getMyCommentVote(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        validateUser(userDetails);
        return ResponseEntity.ok(voteService.getUserCommentVote(commentId, userDetails.getId()));
    }

    private void validateUser(CustomUserDetails userDetails) {
        ValidationUtil.validateUserAuthentication(userDetails);
    }
}
