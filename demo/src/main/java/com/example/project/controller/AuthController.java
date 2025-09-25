package com.example.project.controller;

import com.example.project.entity.req.LoginRequest;
import com.example.project.entity.req.RegisterRequest;
import com.example.project.entity.res.TokenResponse;
import com.example.project.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public TokenResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    public String me() {
        return "Profil bilgileri (giriş yapan kullanıcı)";
    }
}
