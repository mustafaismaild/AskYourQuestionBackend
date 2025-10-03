package com.example.project.entity.res;

import com.example.project.enums.Role;
import java.util.List;

public class UserResponse extends BaseResponse {
    private Long id;
    private String username;
    private String email;
    private boolean status;
    private List<QuestionResponse> questions;
    private List<SavedQuestionResponse> savedQuestions;
    private List<Role> roles;
    private String avatarUrl;

    public UserResponse(Long id,
                        String username,
                        String email,
                        List<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.questions = questions;
        this.savedQuestions = savedQuestions;
        this.roles = roles;
    }
    public UserResponse() {

    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public List<QuestionResponse> getQuestions() { return questions; }
    public void setQuestions(List<QuestionResponse> questions) { this.questions = questions; }

    public List<SavedQuestionResponse> getSavedQuestions() { return savedQuestions; }
    public void setSavedQuestions(List<SavedQuestionResponse> savedQuestions) { this.savedQuestions = savedQuestions; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
