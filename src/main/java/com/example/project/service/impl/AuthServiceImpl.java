package com.example.project.service.impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.project.entity.Badge;
import com.example.project.entity.User;
import com.example.project.entity.req.LoginRequest;
import com.example.project.entity.req.RegisterRequest;
import com.example.project.entity.res.TokenResponse;
import com.example.project.entity.res.UserResponse;
import com.example.project.enums.Role;
import com.example.project.exception.BusinessException;
import com.example.project.repository.BadgeRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtUtil;
import com.example.project.service.AuthService;
import com.example.project.util.ValidationUtil;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final BadgeRepository badgeRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           BadgeRepository badgeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.badgeRepository = badgeRepository;
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
        
        // Assign Bronze badge to new user
        Badge bronzeBadge = badgeRepository.findByName("Bronze")
                .orElseThrow(() -> new RuntimeException("Bronze badge not found. Please run data migration first."));
        
        user.setBadges(new HashSet<>());
        user.getBadges().add(bronzeBadge);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        return new TokenResponse(token, user.getId(), user.getUsername(), user.getRoles());
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        // Input validation
        ValidationUtil.validateString(request.username(), "Kullanıcı adı");
        ValidationUtil.validateString(request.password(), "Şifre");

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(
                    "Kullanıcı adı veya şifre hatalı",
                    "Invalid Credentials",
                    HttpStatus.UNAUTHORIZED
                ));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(
                "Kullanıcı adı veya şifre hatalı",
                "Invalid Credentials",
                HttpStatus.UNAUTHORIZED
            );
        }

        if (!user.isStatus()) {
            throw new BusinessException(
                "Hesabınız aktif değil. Lütfen yönetici ile iletişime geçin",
                "Account Disabled",
                HttpStatus.FORBIDDEN
            );
        }

        String token = jwtUtil.generateToken(user);

        return new TokenResponse(token, user.getId(), user.getUsername(), user.getRoles());
    }

    @Override
    public UserResponse meFromToken(String token) {
        String username = jwtUtil.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                    "Kullanıcı bulunamadı",
                    "User Not Found",
                    HttpStatus.NOT_FOUND
                ));

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
