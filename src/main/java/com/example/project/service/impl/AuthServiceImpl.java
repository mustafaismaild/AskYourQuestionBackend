package com.example.project.service.impl;

import com.example.project.entity.User;
import com.example.project.entity.res.UserResponse;
import com.example.project.enums.Role;
import com.example.project.entity.req.LoginRequest;
import com.example.project.entity.req.RegisterRequest;
import com.example.project.entity.res.TokenResponse;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtUtil;
import com.example.project.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRoles(List.of(Role.USER)); // Default USER rolü

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        return new TokenResponse(token, user.getId(), user.getUsername(), user.getRoles());
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);

        return new TokenResponse(token, user.getId(), user.getUsername(), user.getRoles());
    }

    @Override
    public UserResponse meFromToken(String token) {
        String username = jwtUtil.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Role> roles = jwtUtil.extractRoles(token);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoles(roles);
        response.setStatus(user.isStatus());

        return response;
    }
}
