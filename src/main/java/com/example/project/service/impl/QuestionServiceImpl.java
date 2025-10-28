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
import com.example.project.service.TagService;
import com.example.project.service.BadgeService;
import com.example.project.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;
    private final VoteRepository voteRepository;
    private final TagService tagService;
    private final BadgeService badgeService;
    private final NotificationService notificationService;

    public QuestionServiceImpl(UserRepository userRepository, 
                              QuestionRepository questionRepository,
                              TagRepository tagRepository,
                              VoteRepository voteRepository,
                              TagService tagService,
                              BadgeService badgeService,
                              NotificationService notificationService) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.tagRepository = tagRepository;
        this.voteRepository = voteRepository;
        this.tagService = tagService;
        this.badgeService = badgeService;
        this.notificationService = notificationService;
    }

    private List<Tag> resolveTags(List<String> tagNames) {
        List<Tag> tagList = new ArrayList<>();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setQuestionCount(0);
                            newTag.setStatus(true);
                            newTag.setCreatedAt(LocalDateTime.now());
                            return tagRepository.save(newTag);
                        });
                tagList.add(tag);
            }
        }
        return tagList;
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

    @Override
    public QuestionResponse saveQuestion(QuestionRequest request, Long userId , String userName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setUser(user);
        question.setStatus(true);
        question.setCreatedAt(LocalDateTime.now());

        // Tag'leri ekle
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<Tag> tags = resolveTags(request.getTags());
            question.setTags(tags);
        }

        questionRepository.save(question);

        // Tag sayılarını güncelle
        if (question.getTags() != null) {
            for (Tag tag : question.getTags()) {
                tagService.incrementQuestionCount(tag.getId());
            }
        }

        // Kullanıcının soru sayısını artır ve badge'lerini güncelle
        if (user.getQuestionCount() == null) {
            user.setQuestionCount(0);
        }
        user.setQuestionCount(user.getQuestionCount() + 1);
        userRepository.save(user);
        
        // Badge'leri güncelle
        badgeService.updateUserBadges(user);

        // Soru kaydedildi bildirimi gönder
        // TODO: Fix database constraint for notifications_type_check
        // notificationService.notifyQuestionSaved(user, question.getId());

        // Mention tespiti yap
        detectAndNotifyMentions(question.getContent(), user, question.getId(), "QUESTION");

        // Base64 image işlemi
        if (request.getImageBase64() != null && !request.getImageBase64().trim().isEmpty()) {
            try {
                // Base64'ü byte array'e çevir
                byte[] imageBytes = Base64.getDecoder().decode(request.getImageBase64());
                
                // Question'a image bilgilerini ekle
                question.setFileData(imageBytes);
                question.setFileName("image_" + question.getId());
                question.setFileType(request.getImageType() != null ? request.getImageType() : "image/jpeg");
                question.setFileSize((long) imageBytes.length);
                question.setOriginalFileName("image_" + question.getId());

                questionRepository.save(question); // Image bilgilerini güncelle
            } catch (Exception e) {
                throw new RuntimeException("Base64 image işleme hatası: " + e.getMessage());
            }
        }

        return mapToResponse(question, userId);
    }

    @Override
    public QuestionResponse saveQuestionWithImage(QuestionRequest request, Long userId, String userName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setUser(user);
        question.setStatus(true);
        question.setCreatedAt(LocalDateTime.now());

        // Tag'leri ekle
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            question.setTags(resolveTags(request.getTags()));
        }

        // Önce question'ı kaydet ki ID'yi alabilelim
        questionRepository.save(question);

        // Tag sayılarını güncelle
        if (question.getTags() != null) {
            for (Tag tag : question.getTags()) {
                tagService.incrementQuestionCount(tag.getId());
            }
        }

        // Kullanıcının soru sayısını artır ve badge'lerini güncelle
        if (user.getQuestionCount() == null) {
            user.setQuestionCount(0);
        }
        user.setQuestionCount(user.getQuestionCount() + 1);
        userRepository.save(user);
        
        // Badge'leri güncelle
        badgeService.updateUserBadges(user);

        // Soru kaydedildi bildirimi gönder
        // TODO: Fix database constraint for notifications_type_check
        // notificationService.notifyQuestionSaved(user, question.getId());

        // Mention tespiti yap
        detectAndNotifyMentions(question.getContent(), user, question.getId(), "QUESTION");

        // File upload işlemi - Byte array olarak kaydet
        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            try {
                // Dosyayı byte array'e çevir
                byte[] fileData = file.getBytes();
                
                // Question'a dosya bilgilerini ekle
                question.setFileData(fileData);
                question.setFileName(file.getOriginalFilename());
                question.setFileType(file.getContentType());
                question.setFileSize(file.getSize());
                question.setOriginalFileName(file.getOriginalFilename());

                questionRepository.save(question); // File bilgilerini güncelle
            } catch (Exception e) {
                throw new RuntimeException("Dosya yükleme hatası: " + e.getMessage());
            }
        }

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
            // Tag sayılarını azalt
            if (q.getTags() != null) {
                for (Tag tag : q.getTags()) {
                    tagService.decrementQuestionCount(tag.getId());
                }
            }
            
            q.setStatus(false);
            questionRepository.save(q);
        });
    }

    @Override
    public void markAsSolved(Long questionId, String username) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Soru bulunamadı"));

        Long ownerId = question.getUser() != null ? question.getUser().getId() : null;

        Long requesterId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"))
                .getId();

        if (ownerId == null || !ownerId.equals(requesterId)) {
            throw new RuntimeException("Yetkisiz işlem");
        }

        if (!Boolean.TRUE.equals(question.isSolved())) {
            question.setSolved(true);
            question.setUpdatedAt(java.time.LocalDateTime.now());
            questionRepository.save(question);
        }
    }

    @Override
    public void unsolveQuestion(Long questionId, String username) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Soru bulunamadı"));

        Long ownerId = question.getUser() != null ? question.getUser().getId() : null;

        Long requesterId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"))
                .getId();

        if (ownerId == null || !ownerId.equals(requesterId)) {
            throw new RuntimeException("Yetkisiz işlem");
        }

        if (Boolean.TRUE.equals(question.isSolved())) {
            question.setSolved(false);
            question.setUpdatedAt(java.time.LocalDateTime.now());
            questionRepository.save(question);
        }
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

    @Override
    public List<QuestionResponse> getQuestionsByTagName(String tagName) {
        List<Question> questions = questionRepository.findByTagName(tagName);
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public List<QuestionResponse> getQuestionsByTagId(Long tagId) {
        List<Question> questions = questionRepository.findByTagId(tagId);
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public List<QuestionResponse> getQuestionsByUser(Long userId) {
        List<Question> questions = questionRepository.findByUserIdAndStatus(userId, true);
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, userId));
        }
        return responseList;
    }

    // ✅ Yeni eklenen metodlar
    @Override
    public List<QuestionResponse> getMostViewedQuestions() {
        List<Question> questions = questionRepository.findMostViewed();
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public List<QuestionResponse> getMostVisitedQuestions() {
        // ✅ En çok görüntülenen soruları "sık ziyaret edilenler" olarak döndür
        List<Question> questions = questionRepository.findMostViewed();
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public List<QuestionResponse> getLatestQuestions() {
        List<Question> questions = questionRepository.findLatest();
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public List<QuestionResponse> getUnansweredQuestions() {
        List<Question> questions = questionRepository.findUnanswered();
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public List<QuestionResponse> getRecentlySolvedQuestions() {
        List<Question> questions = questionRepository.findRecentlySolved();
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public List<QuestionResponse> fullTextSearch(String searchTerm) {
        List<Question> questions = questionRepository.fullTextSearch(searchTerm);
        List<QuestionResponse> responseList = new ArrayList<>();
        for (Question q : questions) {
            responseList.add(mapToResponse(q, null));
        }
        return responseList;
    }

    @Override
    public void incrementViewCount(Long questionId) {
        questionRepository.incrementViewCount(questionId);
    }

    private QuestionResponse mapToResponse(Question question, Long userId) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setUserName(question.getUser().getUsername());
        response.setUserAvatarUrl("/api/users/" + question.getUser().getId() + "/avatar");
        response.setTitle(question.getTitle());
        response.setContent(question.getContent());
        response.setFileName(question.getFileName());
        response.setFileUrl(question.getFileUrl());
        response.setImageUrl(question.getFileUrl()); // Backward compatibility
        response.setFileType(question.getFileType());
        response.setFileSize(question.getFileSize());
        response.setOriginalFileName(question.getOriginalFileName());
        response.setHasFile(question.getFileName() != null && !question.getFileName().isEmpty());
        
        // Byte array'i Base64'e çevir
        if (question.getFileData() != null && question.getFileData().length > 0) {
            String base64Data = Base64.getEncoder().encodeToString(question.getFileData());
            response.setFileDataBase64(base64Data);
        }
        
        response.setSolved(question.isSolved());
        response.setStatus(question.isStatus());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());
        response.setUserId(question.getUser() != null ? question.getUser().getId() : null);
        
        // ✅ Yeni eklenen alanlar
        response.setVoteCount(question.getVoteCount() != null ? question.getVoteCount().intValue() : 0);
        response.setUpvoteCount(question.getUpvoteCount() != null ? question.getUpvoteCount().intValue() : 0);
        response.setDownvoteCount(question.getDownvoteCount() != null ? question.getDownvoteCount().intValue() : 0);
        response.setViewCount(question.getViewCount() != null ? question.getViewCount().intValue() : 0);

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
                ansResp.setUsername(answer.getUser() != null ? answer.getUser().getUsername() : null);
                ansResp.setUserAvatarUrl(answer.getUser() != null ? "/api/users/" + answer.getUser().getId() + "/avatar" : null);
                ansResp.setCreatedAt(answer.getCreatedAt());
                ansResp.setUpdatedAt(answer.getUpdatedAt());
                ansResp.setVoteCount(answer.getVoteCount() != null ? answer.getVoteCount().intValue() : 0);
                ansResp.setUpvoteCount(answer.getUpvoteCount() != null ? answer.getUpvoteCount().intValue() : 0);
                ansResp.setDownvoteCount(answer.getDownvoteCount() != null ? answer.getDownvoteCount().intValue() : 0);
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
