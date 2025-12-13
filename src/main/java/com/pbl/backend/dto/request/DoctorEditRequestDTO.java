package com.pbl.backend.dto.request;

import lombok.Data;

@Data
public class DoctorEditRequestDTO {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String position;
    private String degree;
    private String specialty;
}