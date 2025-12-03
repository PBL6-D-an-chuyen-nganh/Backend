package com.pbl.backend.dto.response;

import com.pbl.backend.model.Doctor;
import lombok.Data;

@Data
public class DoctorSummaryDTO {
    private Long userId;
    private String name;
    private String position;
    private String degree;
    private String specialty;

    public static DoctorSummaryDTO fromEntity(Doctor doctor) {
        DoctorSummaryDTO dto = new DoctorSummaryDTO();
        dto.setUserId(doctor.getUserId());
        dto.setName(doctor.getName());
        dto.setPosition(doctor.getPosition());
        dto.setDegree(doctor.getDegree());
        dto.setSpecialty(doctor.getSpecialty());
        return dto;
    }
}
