package com.pbl.backend.dto.request;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String name;
    private String phoneNumber;
}
