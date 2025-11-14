package com.pbl.backend.dto;

import lombok.Data;

@Data
public class PatientSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
}