package ru.rapidcoder.gemini.database.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "gemini_api_key_id")
    private String geminiApiKeyId;

    @Column(name = "qwen_api_key_id")
    private String qwenApiKeyId;

    public User() {
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getGeminiApiKeyId() {
        return geminiApiKeyId;
    }

    public void setGeminiApiKeyId(String geminiApiKeyId) {
        this.geminiApiKeyId = geminiApiKeyId;
    }

    public String getQwenApiKeyId() {
        return qwenApiKeyId;
    }

    public void setQwenApiKeyId(String qwenApiKeyId) {
        this.qwenApiKeyId = qwenApiKeyId;
    }
}
