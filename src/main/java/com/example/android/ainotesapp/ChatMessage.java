package com.example.android.ainotesapp;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private long timestamp;

    public ChatMessage() {
        // Empty constructor for Firebase
    }

    public ChatMessage(String message, boolean isUser, long timestamp) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
