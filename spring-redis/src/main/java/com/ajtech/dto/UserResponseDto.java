package com.ajtech.dto;

import java.time.LocalDateTime;

public class UserResponseDto {
    private String id;
    private String name;
    private String email;
    private String status;

    public UserResponseDto(String id, String email, String name, String status) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
