package com.example.project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.project.entity.req.ChangePasswordRequest;
import com.example.project.entity.res.TokenResponse;
import com.example.project.entity.res.UserResponse;
import com.example.project.entity.req.LoginRequest;
import com.example.project.entity.req.RegisterRequest;
import com.example.project.exception.BusinessException;
import com.example.project.security.JwtUtil;
import com.example.project.service.AuthService;
import com.example.project.service.PasswordResetService;
import com.example.project.service.UserService;
import com.example.project.util.ValidationUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;
    private final UserService userService;

    public AuthController(PasswordEncoder passwordEncoder,
                          AuthService authService,
                          JwtUtil jwtUtil,
                          PasswordResetService passwordResetService,
                          UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
        this.userService = userService;
    }

    // ✅ Register
    @PostMapping("/register")
    public TokenResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // ✅ Login
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // ✅ Kullanıcı Bilgileri (JWT’den)
    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    public UserResponse me(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(
                "Authorization header eksik veya geçersiz",
                "Invalid Authorization Header",
                org.springframework.http.HttpStatus.UNAUTHORIZED
            );
        }
        String token = header.substring(7);
        return authService.meFromToken(token);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        passwordResetService.createPasswordResetToken(email);
        return ResponseEntity.ok("Password reset token sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                                @RequestParam String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request,
                                                 Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = authentication.getName();

            // Eski şifreyi doğrula
            String currentHashedPassword = userService.getUserByUsername(username).getPassword();
            if (!passwordEncoder.matches(request.getOldPassword(), currentHashedPassword)) {
                return ResponseEntity.badRequest().body("Mevcut şifre hatalı");
            }

            // Yeni şifreyi hashle ve kaydet
            String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
            userService.updatePassword(username, hashedNewPassword);

            return ResponseEntity.ok("Şifre başarıyla değiştirildi");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Şifre değiştirilemedi");
        }
    }
}