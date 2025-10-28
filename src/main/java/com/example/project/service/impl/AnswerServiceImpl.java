package com.example.project.service.impl;

import com.example.project.entity.Answer;
import com.example.project.entity.Question;
import com.example.project.entity.User;
import com.example.project.entity.req.AnswerRequest;
import com.example.project.entity.res.AnswerResponse;
import com.example.project.repository.AnswerRepository;
import com.example.project.repository.QuestionRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.AnswerService;
import com.example.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final NotificationService notificationService;

    @Override
    public AnswerResponse createAnswer(AnswerRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Answer answer = new Answer();
        answer.setContent(request.getContent());
        answer.setUser(user);
        answer.setQuestion(question);
        answer.setStatus(true);
        answer.setCreatedAt(LocalDateTime.now());

        Answer savedAnswer = answerRepository.save(answer);
        
        // Bildirim gönder - sadece kendi sorusuna cevap vermiyorsa
        if (!question.getUser().getId().equals(userId)) {
            notificationService.notifyNewAnswer(question.getUser(), question.getId());
        }

        // Mention tespiti yap
        detectAndNotifyMentions(answer.getContent(), user, savedAnswer.getId(), "ANSWER");

        return mapToResponse(savedAnswer);
    }

    @Override
    public List<AnswerResponse> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionIdAndStatus(questionId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnswerResponse> getAnswersByUser(Long userId) {
        return answerRepository.findByUserIdAndStatus(userId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public void acceptAnswer(Long id) {
        answerRepository.findById(id).ifPresent(answer -> {
            answer.setAccepted(true);
            answer.setUpdatedAt(LocalDateTime.now());
            answerRepository.save(answer);

            Question question = answer.getQuestion();
            if (question != null) {
                question.setSolved(true);
            }
            
            // Cevap sahibine bildirim gönder
            notificationService.notifyAnswerAccepted(answer.getUser(), answer.getId());
        });
    }

    @Override
    public void deleteAcceptAnswer(Long id) {
        answerRepository.findById(id).ifPresent(answer -> {
            answer.setAccepted(false);
            answer.setUpdatedAt(LocalDateTime.now());
            answerRepository.save(answer);

            Question question = answer.getQuestion();
            if (question != null) {
                question.setSolved(false);
            }
        });
    }

    @Override
    public void deleteAnswer(Long id) {
        answerRepository.findById(id).ifPresent(answer -> {
            answer.setStatus(false);
            answer.setUpdatedAt(LocalDateTime.now());
            answerRepository.save(answer);
        });
    }

    @Override
    public Optional<Answer> getAnswerById(Long id) {
        return answerRepository.findById(id);
    }

    private void detectAndNotifyMentions(String content, User author, Long entityId, String entityType) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        Matcher matcher = mentionPattern.matcher(content);
        
        while (matcher.find()) {
            String mentionedUsername = matcher.group(1);
            userRepository.findByUsername(mentionedUsername).ifPresent(mentionedUser -> {
                // Kendi kendini bahsetmiyorsa bildirim gönder
                if (!mentionedUser.getId().equals(author.getId())) {
                    notificationService.notifyMention(mentionedUser, author, entityId, entityType);
                }
            });
        }
    }

    private AnswerResponse mapToResponse(Answer answer) {
        AnswerResponse response = new AnswerResponse();
        response.setId(answer.getId());
        response.setContent(answer.getContent());
        response.setCreatedAt(answer.getCreatedAt());
        response.setUpdatedAt(answer.getUpdatedAt());
        response.setAccepted(answer.isAccepted());

        if (answer.getUser() != null) {
            response.setUserId(answer.getUser().getId());
            response.setUsername(answer.getUser().getUsername());
            response.setUserAvatarUrl("/api/users/" + answer.getUser().getId() + "/avatar");
        }

        if (answer.getQuestion() != null) {
            response.setQuestionId(answer.getQuestion().getId());
        }

        return response;
    }
}
