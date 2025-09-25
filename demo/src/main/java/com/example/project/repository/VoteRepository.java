package com.example.project.repository;

import com.example.project.entity.Vote;
import com.example.project.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
