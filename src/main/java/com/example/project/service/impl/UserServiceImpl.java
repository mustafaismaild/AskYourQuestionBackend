package com.example.project.service.impl;

import com.example.project.entity.User;
import com.example.project.entity.res.AnswerResponse;
import com.example.project.entity.res.QuestionResponse;
import com.example.project.entity.res.SavedQuestionResponse;
import com.example.project.entity.res.UserResponse;
import com.example.project.enums.Role;
import com.example.project.repository.UserRepository;
import com.example.project.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setStatus(user.isStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        if (user.getQuestions() != null && !user.getQuestions().isEmpty()) {
            response.setQuestions(
                    user.getQuestions().stream()
                            .map(q -> {
                                QuestionResponse qr = new QuestionResponse();
                                qr.setId(q.getId());
                                qr.setTitle(q.getTitle());
                                qr.setContent(q.getContent());
                                qr.setSolved(q.isSolved());
                                qr.setStatus(q.isStatus());
                                qr.setCreatedAt(q.getCreatedAt());
                                qr.setUpdatedAt(q.getUpdatedAt());
                                qr.setUserId(q.getUser() != null ? q.getUser().getId() : null);

                                if (q.getAnswers() != null && !q.getAnswers().isEmpty()) {
                                    List<AnswerResponse> answerResponses = q.getAnswers().stream()
                                            .map(a -> {
                                                AnswerResponse ar = new AnswerResponse();
                                                ar.setId(a.getId());
                                                ar.setContent(a.getContent());
                                                ar.setCreatedAt(a.getCreatedAt());
                                                ar.setUpdatedAt(a.getUpdatedAt());
                                                ar.setUserId(a.getUser() != null ? a.getUser().getId() : null);
                                                return ar;
                                            }).toList();
                                    qr.setAnswers(answerResponses);
                                }

                                if (q.getTags() != null && !q.getTags().isEmpty()) {
                                    qr.setTags(q.getTags().stream()
                                            .map(tag -> tag.getName())
                                            .toList());
                                }

                                return qr;
                            })
                            .collect(Collectors.toList())
            );
        }


        // User -> SavedQuestions
            if (user.getSavedQuestions() != null) {
            response.setSavedQuestions(
                    user.getSavedQuestions().stream()
                            .map(sq -> new SavedQuestionResponse(
                                    sq.getId(),
                                    sq.getUser().getId(),
                                    sq.getQuestion().getId(),
                                    sq.getQuestion().getTitle(),
                                    sq.getCreatedAt()
                            ))
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    @Override
    public UserResponse createUser(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of(Role.USER));
        }
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    public List<UserResponse> getActiveUsers() {
        return userRepository.findByStatus(true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findByIdWithRelations(id)
                .filter(User::isStatus)
                .map(this::toResponse);
    }


    @Override
    public UserResponse updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
