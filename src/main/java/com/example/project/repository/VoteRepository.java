package com.example.project.repository;

import com.example.project.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByUserIdAndQuestionIdAndStatus(Long userId, Long questionId, boolean status);
    List<Vote> findAllByQuestionIdAndStatus(Long questionId , boolean status);

    Optional<Vote> findByUserIdAndAnswerIdAndStatus(Long userId, Long answerId, boolean status);
    List<Vote> findAllByAnswerIdAndStatus(Long answerId , boolean status);

    Optional<Vote> findByUserIdAndCommentIdAndStatus(Long userId, Long commentId, boolean status);
    List<Vote> findAllByCommentIdAndStatus( Long commentId, boolean status);
    @Query("SELECT COALESCE(SUM(v.value), 0) FROM Vote v WHERE v.question.id = :questionId AND v.status = true")
    int sumByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT COALESCE(SUM(v.value), 0) FROM Vote v WHERE v.answer.id = :answerId AND v.status = true")
    int sumByAnswerId(@Param("answerId") Long answerId);

    @Query("SELECT COALESCE(SUM(v.value), 0) FROM Vote v WHERE v.comment.id = :commentId AND v.status = true")
    int sumByCommentId(@Param("commentId") Long commentId);

    // ✅ Vote count metodları
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.question.id = :questionId AND v.status = :status")
    int countByQuestionIdAndStatus(@Param("questionId") Long questionId, @Param("status") boolean status);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.answer.id = :answerId AND v.status = :status")
    int countByAnswerIdAndStatus(@Param("answerId") Long answerId, @Param("status") boolean status);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.comment.id = :commentId AND v.status = :status")
    int countByCommentIdAndStatus(@Param("commentId") Long commentId, @Param("status") boolean status);

    // ✅ Upvote count metodları
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.question.id = :questionId AND v.status = :status AND v.type = 'UPVOTE'")
    int countUpvotesByQuestionIdAndStatus(@Param("questionId") Long questionId, @Param("status") boolean status);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.answer.id = :answerId AND v.status = :status AND v.type = 'UPVOTE'")
    int countUpvotesByAnswerIdAndStatus(@Param("answerId") Long answerId, @Param("status") boolean status);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.comment.id = :commentId AND v.status = :status AND v.type = 'UPVOTE'")
    int countUpvotesByCommentIdAndStatus(@Param("commentId") Long commentId, @Param("status") boolean status);

    // ✅ Downvote count metodları
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.question.id = :questionId AND v.status = :status AND v.type = 'DOWNVOTE'")
    int countDownvotesByQuestionIdAndStatus(@Param("questionId") Long questionId, @Param("status") boolean status);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.answer.id = :answerId AND v.status = :status AND v.type = 'DOWNVOTE'")
    int countDownvotesByAnswerIdAndStatus(@Param("answerId") Long answerId, @Param("status") boolean status);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.comment.id = :commentId AND v.status = :status AND v.type = 'DOWNVOTE'")
    int countDownvotesByCommentIdAndStatus(@Param("commentId") Long commentId, @Param("status") boolean status);

    boolean existsByQuestionIdAndUserId(Long questionId, Long userId);
    boolean existsByAnswerIdAndUserId(Long answerId, Long userId);
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

}
