package com.ajtech.dto;

public class ConversationDto {

    private  String currentUserId;
    private  String userId;

    public ConversationDto(String currentUserId, String userId) {
        this.currentUserId = currentUserId;
        this.userId = userId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
