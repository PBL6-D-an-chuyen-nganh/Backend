package com.pbl.backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private Integer userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private String password;
    private String authStatus;
}

