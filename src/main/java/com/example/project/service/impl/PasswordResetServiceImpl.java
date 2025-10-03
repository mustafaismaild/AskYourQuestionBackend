package com.example.project.service.impl;

import com.example.project.entity.PasswordResetToken;
import com.example.project.entity.User;
import com.example.project.repository.PasswordResetTokenRepository; // Paket adının gerçekten bu olduğundan emin olun
import com.example.project.repository.UserRepository;
import com.example.project.service.PasswordResetService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createPasswordResetToken(String email) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));


        tokenRepository.deleteAllByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        resetToken.setCreatedAt(LocalDateTime.now());

        tokenRepository.save(resetToken);

        // Burada e-posta gönderimi yapılmalı
        // mailService.sendPasswordReset(user.getEmail(), token);
        System.out.println("RESET TOKEN: " + token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate() == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        if (user == null || !user.isStatus()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("User not active or not found");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Kullanılan dahil kullanıcıya ait tüm tokenları temizle
        tokenRepository.deleteAllByUserId(user.getId());
    }
}