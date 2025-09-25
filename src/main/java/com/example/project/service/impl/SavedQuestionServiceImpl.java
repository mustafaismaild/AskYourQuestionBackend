package com.example.project.service.impl;

import com.example.project.entity.Question;
import com.example.project.entity.SavedQuestion;
import com.example.project.entity.User;
import com.example.project.entity.req.SavedQuestionRequest;
import com.example.project.entity.res.SavedQuestionResponse;
import com.example.project.repository.QuestionRepository;
import com.example.project.repository.SavedQuestionRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.SavedQuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedQuestionServiceImpl implements SavedQuestionService {

    private final SavedQuestionRepository savedQuestionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public SavedQuestionServiceImpl(SavedQuestionRepository savedQuestionRepository,
                                    UserRepository userRepository,
                                    QuestionRepository questionRepository) {
        this.savedQuestionRepository = savedQuestionRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public SavedQuestionResponse saveQuestion(Long userId, SavedQuestionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (savedQuestionRepository.existsByUserIdAndQuestionId(userId, question.getId())) {
            throw new RuntimeException("Already saved!");
        }

        SavedQuestion saved = new SavedQuestion();
        saved.setUser(user);
        saved.setQuestion(question);

        SavedQuestion persisted = savedQuestionRepository.save(saved);

        return new SavedQuestionResponse(
                persisted.getId(),
                user.getId(),
                question.getId(),
                question.getTitle(),
                persisted.getCreatedAt()
        );
    }

    @Override
    public List<SavedQuestionResponse> getSavedQuestions(Long userId) {
        return savedQuestionRepository.findByUserId(userId).stream()
                .map(saved -> new SavedQuestionResponse(
                        saved.getId(),
                        saved.getUser().getId(),
                        saved.getQuestion().getId(),
                        saved.getQuestion().getTitle(),
                        saved.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public void deleteSavedQuestion(Long userId, Long savedId) {
        SavedQuestion saved = savedQuestionRepository.findById(savedId)
                .orElseThrow(() -> new RuntimeException("Saved question not found"));
        if (!saved.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this saved question");
        }
        savedQuestionRepository.delete(saved);
    }
}
