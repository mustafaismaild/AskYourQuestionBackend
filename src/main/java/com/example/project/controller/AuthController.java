package com.example.project.controller;

import com.example.project.entity.res.TokenResponse;
import com.example.project.entity.res.UserResponse;
import com.example.project.entity.req.LoginRequest;
import com.example.project.entity.req.RegisterRequest;
import com.example.project.security.JwtUtil;
import com.example.project.service.AuthService;
import com.example.project.service.PasswordResetService;
import com.example.project.service.impl.PasswordResetServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, JwtUtil jwtUtil, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
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
            throw new RuntimeException("Authorization header missing or invalid");
        }

        String token = header.substring(7);
        return authService.meFromToken(token);
    }

    // ✅ Şifremi Unuttum
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        passwordResetService.createPasswordResetToken(email);
        return ResponseEntity.ok("Password reset token sent to your email");
    }

    // ✅ Şifre Sıfırla
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                                @RequestParam String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password has been reset successfully");
    }
}
