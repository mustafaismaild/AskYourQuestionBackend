package com.example.project.entity.req;

public record RegisterRequest(
        String username,
        String password,
        String email
) {}