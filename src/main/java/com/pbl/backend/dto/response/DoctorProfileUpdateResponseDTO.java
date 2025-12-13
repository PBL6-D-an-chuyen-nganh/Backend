package com.pbl.backend.dto.response;

import lombok.Data;

@Data
public class DoctorProfileUpdateResponseDTO {
    private Long userId;
    private String name;
    private String phoneNumber;
    private Integer yoe;
    private String introduction;
    private String degree;
    private String achievements;

    public static DoctorProfileUpdateResponseDTO fromEntity(com.pbl.backend.model.Doctor doctor) {
        DoctorProfileUpdateResponseDTO dto = new DoctorProfileUpdateResponseDTO();
        dto.setUserId(doctor.getUserId());
        dto.setName(doctor.getName());
        dto.setPhoneNumber(doctor.getPhoneNumber());
        dto.setYoe(doctor.getYoe());
        dto.setIntroduction(doctor.getIntroduction());
        dto.setDegree(doctor.getDegree());
        dto.setAchievements(doctor.getAchievements());
        return dto;
    }
}