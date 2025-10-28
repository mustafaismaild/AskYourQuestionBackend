package com.example.project.controller;

import com.example.project.entity.CustomUserDetails;
import com.example.project.entity.Question;
import com.example.project.entity.req.QuestionRequest;
import com.example.project.entity.res.QuestionResponse;
import com.example.project.mapper.QuestionMapper;
import com.example.project.service.QuestionService;
import com.example.project.util.AdminUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // 🟩 Test endpoint - basit soru kaydetme
    @PostMapping("/test-save")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<String> testSave(
            @RequestBody QuestionRequest questionRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            return ResponseEntity.ok("Test başarılı - User ID: " + userDetails.getId() + 
                    ", Title: " + questionRequest.getTitle() + 
                    ", Content: " + questionRequest.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Test hatası: " + e.getMessage());
        }
    }

    // 🟩 Çok basit test endpoint - hiçbir dependency yok
    @PostMapping("/simple-test")
    public ResponseEntity<String> simpleTest() {
        try {
            return ResponseEntity.ok("Backend çalışıyor - " + System.currentTimeMillis());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hata: " + e.getMessage());
        }
    }

    // 🟩 Soru kaydet (resimsiz veya Base64 image ile)
    @PostMapping("/saveQuestion")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<?> saveQuestion(
            @RequestBody QuestionRequest questionRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            System.out.println("=== SAVE QUESTION DEBUG ===");
            System.out.println("User ID: " + userDetails.getId());
            System.out.println("Username: " + userDetails.getUsername());
            System.out.println("Title: " + questionRequest.getTitle());
            System.out.println("Content: " + questionRequest.getContent());
            System.out.println("Tags: " + questionRequest.getTags());
            System.out.println("ImageBase64: " + (questionRequest.getImageBase64() != null ? "VAR" : "YOK"));
            System.out.println("ImageType: " + questionRequest.getImageType());
            
            QuestionResponse response = questionService.saveQuestion(
                    questionRequest,
                    userDetails.getId(),
                    userDetails.getUsername()
            );
            
            System.out.println("Question saved successfully with ID: " + response.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== SAVE QUESTION ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Soru kaydedilemedi",
                        "message", e.getMessage(),
                        "details", e.getClass().getSimpleName()
                    ));
        }
    }

    // 🟩 Soru kaydet (resimli)
    @PostMapping(value = "/saveQuestionWithImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<QuestionResponse> saveQuestionWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            QuestionRequest questionRequest = new QuestionRequest();
            questionRequest.setTitle(title);
            questionRequest.setContent(content);
            questionRequest.setTags(tags);
            questionRequest.setFile(image);

            QuestionResponse response = questionService.saveQuestionWithImage(
                    questionRequest,
                    userDetails.getId(),
                    userDetails.getUsername()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // 🟩 Soru kaydet (dosya ile) - Yeni endpoint
    @PostMapping(value = "/saveQuestionWithFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<QuestionResponse> saveQuestionWithFile(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            QuestionRequest questionRequest = new QuestionRequest();
            questionRequest.setTitle(title);
            questionRequest.setContent(content);
            questionRequest.setTags(tags);
            questionRequest.setFile(file);

            QuestionResponse response = questionService.saveQuestionWithImage(
                    questionRequest,
                    userDetails.getId(),
                    userDetails.getUsername()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 🟩 Tüm aktif soruları getir
    @GetMapping("/getAll")
    public ResponseEntity<List<QuestionResponse>> getActiveQuestions() {
        return ResponseEntity.ok(questionService.getActiveQuestions());
    }

    // 🟩 Belirli kullanıcının sorularını getir
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(questionService.getQuestionsByUser(userId));
    }

    // 🟩 Giriş yapan kullanıcının sorularını getir
    @GetMapping("/my-questions")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<List<QuestionResponse>> getMyQuestions(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(questionService.getQuestionsByUser(userDetails.getId()));
    }

    // 🟩 ID’ye göre soru getir
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id)
                .map(q -> ResponseEntity.ok(QuestionMapper.toResponse(q)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🟩 Soru güncelle
    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Question question = questionService.getQuestionById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        // Sadece soru sahibi veya admin bu işlemi yapabilir
        if (!AdminUtil.canModifyContent(question.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        Question updated = questionService.updateQuestion(question, request.getTags());
        return ResponseEntity.ok(QuestionMapper.toResponse(updated));
    }

    // 🟩 Soru arama
    @GetMapping("/search")
    public ResponseEntity<List<QuestionResponse>> searchSimilarQuestions(@RequestParam String text) {
        return ResponseEntity.ok(questionService.searchSimilarQuestions(text));
    }

    // 🟩 En çok görüntülenen
    @GetMapping("/most-viewed")
    public ResponseEntity<List<QuestionResponse>> getMostViewedQuestions() {
        return ResponseEntity.ok(questionService.getMostViewedQuestions());
    }

    // 🟩 En çok ziyaret edilen
    @GetMapping("/most-visited")
    public ResponseEntity<List<QuestionResponse>> getMostVisitedQuestions() {
        return ResponseEntity.ok(questionService.getMostVisitedQuestions());
    }

    // 🟩 En son eklenen
    @GetMapping("/latest")
    public ResponseEntity<List<QuestionResponse>> getLatestQuestions() {
        return ResponseEntity.ok(questionService.getLatestQuestions());
    }

    // 🟩 Cevapsız sorular
    @GetMapping("/unanswered")
    public ResponseEntity<List<QuestionResponse>> getUnansweredQuestions() {
        return ResponseEntity.ok(questionService.getUnansweredQuestions());
    }

    // 🟩 Yeni çözülen sorular
    @GetMapping("/recently-solved")
    public ResponseEntity<List<QuestionResponse>> getRecentlySolvedQuestions() {
        return ResponseEntity.ok(questionService.getRecentlySolvedQuestions());
    }

    // 🟩 Full text search
    @GetMapping("/full-text-search")
    public ResponseEntity<List<QuestionResponse>> fullTextSearch(@RequestParam String searchTerm) {
        return ResponseEntity.ok(questionService.fullTextSearch(searchTerm));
    }

    // 🟩 Görüntüleme sayısını artır
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        questionService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    // 🟩 Soru sil (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Question question = questionService.getQuestionById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        // Admin ise veya soru sahibi ise silme işlemini yap
        if (!AdminUtil.canDeleteContent(question.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    // 🟩 Soru çözülmüş olarak işaretle
    @PutMapping("/{id}/solve")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Void> markAsSolved(@PathVariable Long id, Authentication auth) {
        questionService.markAsSolved(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    // 🟩 Çözülmüş işaretini kaldır
    @DeleteMapping("/{id}/solve")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Void> unsolveQuestion(@PathVariable Long id, Authentication auth) {
        questionService.unsolveQuestion(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // 🟩 Image endpoint (frontend için) - Byte array'den oku
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getQuestionImage(@PathVariable Long id) {
        try {
            Question question = questionService.getQuestionById(id)
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            if (question.getFileData() == null || question.getFileData().length == 0) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = question.getFileData();
            String contentType = question.getFileType() != null ? question.getFileType() : "image/jpeg";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
