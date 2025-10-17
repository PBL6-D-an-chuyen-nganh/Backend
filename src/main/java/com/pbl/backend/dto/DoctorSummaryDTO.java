package com.pbl.backend.dto;

import lombok.Data;

@Data
public class DoctorSummaryDTO {
    private Long userId;
    private String name;
    private String position;
    private String degree;
}