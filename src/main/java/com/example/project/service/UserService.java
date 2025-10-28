package com.example.project.service;

import com.example.project.entity.res.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponse createUser(com.example.project.entity.User user);
    List<UserResponse> getActiveUsers();
    Optional<UserResponse> getUserById(Long id);
    UserResponse updateUser(com.example.project.entity.User user);
    void deleteUser(Long id);
    com.example.project.entity.User getUserByUsername(String username);
    void updatePassword(String username, String encodedPassword);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    UserResponse updateAvatar(Long userId, String avatarUrl);
    UserResponse updateAvatarWithFile(Long userId, String fileName);

}