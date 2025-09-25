package com.example.project.entity.res;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionResponse {

    private Long id;
    private String title;
    private String content;
    private String fileName;
    private boolean solved;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;

    private List<AnswerResponse> answers;
    private List<String> tags;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSolved() {
        return solved;
    }

    public boolean isStatus() {
        return status;
    }

    public void setSolved(boolean solved) {
        this.solved = solved; }
    public void getSolved(boolean solved) {
        this.solved = solved; }

    public boolean getStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<AnswerResponse> getAnswers() { return answers; }
    public void setAnswers(List<AnswerResponse> answers) { this.answers = answers; }
}
