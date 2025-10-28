package com.example.project.controller;

import com.example.project.entity.res.AvatarResponse;
import com.example.project.entity.res.BadgeResponse;
import com.example.project.entity.res.UserResponse;
import com.example.project.repository.UserRepository;
import com.example.project.entity.CustomUserDetails;
import com.example.project.service.UserService;
import com.example.project.util.AdminUtil;
import com.example.project.enums.DefaultAvatar;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody com.example.project.entity.User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // @ ile başlayan aramalar için @ işaretini kaldır
        String searchQuery = query.startsWith("@") ? query.substring(1) : query;
        
        List<com.example.project.entity.User> users = userRepository.findByUsernameContainingIgnoreCaseAndStatus(searchQuery, true);
        List<UserResponse> userResponses = users.stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setEmail(user.getEmail());
                    response.setBio(user.getBio());
                    response.setAvatarUrl(user.getAvatarUrl());
                    response.setQuestionCount(user.getQuestionCount());
                    response.setReputation(user.getReputation());
                    response.setCreatedAt(user.getCreatedAt());
                    response.setUpdatedAt(user.getUpdatedAt());
                    return response;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getUserById(userDetails.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/badges")
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    List<BadgeResponse> badges = user.getBadges().stream()
                            .map(badge -> new BadgeResponse(
                                    badge.getId(),
                                    badge.getName(),
                                    badge.getDescription(),
                                    badge.getType(),
                                    badge.getIconUrl(),
                                    badge.getRequiredCount()
                            ))
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(badges);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/me/avatar")
    @SecurityRequirement(name = "BearerAuth")
    public UserResponse updateMyAvatar(Authentication authentication,
                                       @RequestBody Map<String, String> body) {
        String username = authentication.getName();
        Long userId = userService.getUserByUsername(username).getId();
        String avatarUrl = body.get("avatarUrl");
        
        // Avatar URL'yi doğru formata çevir
        if (avatarUrl != null && !avatarUrl.startsWith("/api/users/")) {
            avatarUrl = "/api/users/" + userId + "/avatar";
        }
        
        return userService.updateAvatar(userId, avatarUrl);
    }

    @PostMapping(value = "/me/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<UserResponse> uploadMyAvatar(Authentication authentication,
                                                       @RequestParam("avatar") MultipartFile avatar) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            
            if (avatar.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // File type kontrolü
            if (!isImageFile(avatar)) {
                return ResponseEntity.badRequest().build();
            }
            
            // Dosyayı kaydet
            String fileName = storeAvatarFile(avatar, userId);
            
            // User'ı güncelle
            UserResponse response = userService.updateAvatarWithFile(userId, fileName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody com.example.project.entity.User user,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Sadece kendi hesabını güncelleyebilir veya admin herkesi güncelleyebilir
        if (!AdminUtil.canModifyContent(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return userService.getUserById(id)
                .map(u -> {
                    user.setId(id);
                    return ResponseEntity.ok(userService.updateUser(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Admin ise veya kendi hesabını silebilir
        if (!AdminUtil.canDeleteContent(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable Long id) {
        try {
            Optional<com.example.project.entity.User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                com.example.project.entity.User user = userOpt.get();

                // Check if user has an external avatar URL
                if (user.getAvatarUrl() != null && user.getAvatarUrl().startsWith("http")) {
                    // Redirect to external URL
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .header(HttpHeaders.LOCATION, user.getAvatarUrl())
                            .build();
                }

                // Handle file-based avatars (your existing logic)
                if (user.getAvatarFileName() != null && !user.getAvatarFileName().isEmpty()) {
                    Path filePath = Paths.get("uploads", user.getAvatarFileName());
                    if (Files.exists(filePath)) {
                        byte[] imageBytes = Files.readAllBytes(filePath);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.IMAGE_JPEG);
                        headers.setContentLength(imageBytes.length);

                        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
                    }
                }
                
                // Eğer yüklenen avatar yoksa, varsayılan avatar kullan
                return getDefaultAvatar(user.getId());
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Varsayılan avatar seçeneklerini listele
    @GetMapping("/avatars/default")
    public ResponseEntity<List<AvatarResponse>> getDefaultAvatars() {
        List<AvatarResponse> avatars = Arrays.stream(DefaultAvatar.values())
                .map(avatar -> new AvatarResponse(
                        avatar.name(),
                        avatar.getFileName(),
                        avatar.getDescription(),
                        avatar.getEmoji(),
                        avatar.getUrl(),
                        true
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(avatars);
    }

    // Kullanıcının mevcut avatarını ve seçeneklerini getir
    @GetMapping("/me/avatars")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Map<String, Object>> getMyAvatars(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            
            // Kullanıcının mevcut avatar bilgisi
            Optional<com.example.project.entity.User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            com.example.project.entity.User user = userOpt.get();
            String currentAvatarUrl = user.getAvatarUrl();
            String currentAvatarType = "default";
            
            // Eğer custom avatar varsa
            if (user.getAvatarFileName() != null && !user.getAvatarFileName().isEmpty()) {
                currentAvatarUrl = "/api/users/" + userId + "/avatar";
                currentAvatarType = "custom";
            }
            
            // Tüm default avatar seçenekleri
            List<AvatarResponse> defaultAvatars = Arrays.stream(DefaultAvatar.values())
                    .map(avatar -> new AvatarResponse(
                            avatar.name(),
                            avatar.getFileName(),
                            avatar.getDescription(),
                            avatar.getEmoji(),
                            avatar.getUrl(),
                            true
                    ))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                    "currentAvatar", Map.of(
                            "url", currentAvatarUrl,
                            "type", currentAvatarType
                    ),
                    "defaultAvatars", defaultAvatars
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Varsayılan avatar seç
    @PutMapping("/me/avatar/default")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<UserResponse> selectDefaultAvatar(Authentication authentication,
                                                           @RequestBody Map<String, String> body) {
        try {
            String username = authentication.getName();
            Long userId = userService.getUserByUsername(username).getId();
            String avatarName = body.get("avatarName");
            
            if (avatarName == null || avatarName.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Avatar enum'ını kontrol et
            DefaultAvatar selectedAvatar;
            try {
                selectedAvatar = DefaultAvatar.valueOf(avatarName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
            
            // Avatar URL'yi güncelle
            String avatarUrl = selectedAvatar.getUrl();
            UserResponse response = userService.updateAvatar(userId, avatarUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Varsayılan avatar dosyasını getir
    @GetMapping("/avatars/default/{fileName}")
    public ResponseEntity<byte[]> getDefaultAvatarFile(@PathVariable String fileName) {
        try {
            // Güvenlik kontrolü - sadece belirli dosya isimlerine izin ver
            boolean isValidFile = Arrays.stream(DefaultAvatar.values())
                    .anyMatch(avatar -> avatar.getFileName().equals(fileName));
            
            if (!isValidFile) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = Paths.get("static/avatars", fileName);
            if (Files.exists(filePath)) {
                byte[] imageBytes = Files.readAllBytes(filePath);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(imageBytes.length);
                
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            }
            
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Kullanıcı ID'sine göre varsayılan avatar getir (ID'ye göre mod)
    private ResponseEntity<byte[]> getDefaultAvatar(Long userId) {
        try {
            // Kullanıcı ID'sine göre varsayılan avatar seç (mod 8)
            int avatarIndex = (int) (userId % 8) + 1;
            String fileName = "avatar_" + avatarIndex + ".png";
            
            Path filePath = Paths.get("static/avatars", fileName);
            if (Files.exists(filePath)) {
                byte[] imageBytes = Files.readAllBytes(filePath);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(imageBytes.length);
                
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            }
            
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private String storeAvatarFile(MultipartFile file, Long userId) throws IOException {
        // Uploads dizinini oluştur
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Dosya adını oluştur: avatar_userId_timestamp.extension
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadDir.resolve(fileName);
        
        // Dosyayı kaydet
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
}
