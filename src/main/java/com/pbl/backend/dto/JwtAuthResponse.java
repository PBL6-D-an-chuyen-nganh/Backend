package com.pbl.backend.dto;

import lombok.Data;

@Data
public class JwtAuthResponse {
    private String token;
    private String refreshToken;
    private UserDTO user;
}
