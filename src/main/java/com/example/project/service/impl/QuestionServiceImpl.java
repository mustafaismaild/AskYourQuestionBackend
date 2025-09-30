package com.example.project.service.impl;

import com.example.project.entity.Question;
import com.example.project.entity.Tag;
import com.example.project.entity.User;
import com.example.project.entity.req.QuestionRequest;
import com.example.project.entity.res.AnswerResponse;
import com.example.project.entity.res.QuestionResponse;
import com.example.project.repository.QuestionRepository;
import com.example.project.repository.TagRepository;
import com.example.project.repository.UserRepository;
import com.example.project.repository.VoteRepository;
import com.example.project.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;
    private final VoteRepository voteRepository;

    private List<Tag> resolveTags(List<String> tagNames) {
        List<Tag> tagList = new ArrayList<>();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                tagList.add(tag);
            }
        }
        return tagList;
    }

    @Override
    public QuestionResponse saveQuestion(QuestionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setUser(user);
        question.setStatus(true);
        question.setCreatedAt(LocalDateTime.now());

        questionRepository.save(question);

        return mapToResponse(question, userId);
    }

    @Override
    public Question updateQuestion(Question question, List<String> tagNames) {
        question.setTags(resolveTags(tagNames));
        return questionRepository.save(question);
    }

    @Override
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    @Override
    public List<QuestionResponse> getActiveQuestions() {
        List<Question> questions = questionRepository.findByStatusTrue();
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question q : questions) {
            responses.add(mapToResponse(q, null)); // userId null ise userVoted false olur
        }
        return responses;
    }

    @Override
    public void deleteQuestion(Long id) {
        questionRepository.findById(id).ifPresent(q -> {
            q.setStatus(false);
            questionRepository.save(q);
        });
    }

    @Override
    public void markAsSolved(Question question) {
        question.setSolved(true);
        questionRepository.save(question);
    }

    @Override
    public List<QuestionResponse> searchSimilarQuestions(String text) {
        List<Question> questions = questionRepository.findByTextContainingIgnoreCase(text);
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    private QuestionResponse mapToResponse(Question question, Long userId) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setUserId(Long.valueOf(question.getUser().getUsername()));
        response.setTitle(question.getTitle());
        response.setContent(question.getContent());
        response.setSolved(question.isSolved());
        response.setStatus(question.isStatus());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());
        response.setUserId(question.getUser() != null ? question.getUser().getId() : null);

        // Tag listesi
        if (question.getTags() != null) {
            List<String> tagNames = new ArrayList<>();
            for (Tag tag : question.getTags()) {
                tagNames.add(tag.getName());
            }
            response.setTags(tagNames);
        }

        // Answer listesi
        if (question.getAnswers() != null) {
            List<AnswerResponse> answers = new ArrayList<>();
            for (var answer : question.getAnswers()) {
                AnswerResponse ansResp = new AnswerResponse();
                ansResp.setId(answer.getId());
                ansResp.setContent(answer.getContent());
                ansResp.setUserId(answer.getUser() != null ? answer.getUser().getId() : null);
                ansResp.setCreatedAt(answer.getCreatedAt());
                ansResp.setUpdatedAt(answer.getUpdatedAt());
                answers.add(ansResp);
            }
            response.setAnswers(answers);
        }

        // Oy durumu
        int totalVoteScore = voteRepository.sumByQuestionId(question.getId());
        boolean userVoted = userId != null && voteRepository.existsByQuestionIdAndUserId(question.getId(), userId);
        response.setTotalVoteScore(totalVoteScore);
        response.setUserVoted(userVoted);

        return response;
    }
}
