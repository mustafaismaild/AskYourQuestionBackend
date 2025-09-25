package com.example.project.service;


import com.example.project.entity.req.LoginRequest;
import com.example.project.entity.req.RegisterRequest;
import com.example.project.entity.res.TokenResponse;

public interface AuthService {
    TokenResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
}
