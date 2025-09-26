package com.example.project.entity.res;


import java.time.LocalDateTime;
import java.util.List;

public class UserResponse extends BaseResponse {
    private Long id;
    private String username;
    private boolean status;
    private List<QuestionResponse> questions;
    private List<SavedQuestionResponse> savedQuestions;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }

    public List<SavedQuestionResponse> getSavedQuestions() {
        return savedQuestions;
    }

    public void setSavedQuestions(List<SavedQuestionResponse> savedQuestions) {
        this.savedQuestions = savedQuestions;
    }

}
