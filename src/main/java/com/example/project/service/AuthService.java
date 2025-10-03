package com.example.project.service;


import com.example.project.entity.req.LoginRequest;
import com.example.project.entity.req.RegisterRequest;
import com.example.project.entity.req.UserRequest;
import com.example.project.entity.res.TokenResponse;
import com.example.project.entity.res.UserResponse;

public interface AuthService {
    TokenResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    UserResponse meFromToken(String token);

}
