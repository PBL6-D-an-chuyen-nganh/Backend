package com.pbl.backend.dto.request;

import lombok.Data;

@Data
public class JwtAuthRequest {
    private String email;
    private String password;
}
