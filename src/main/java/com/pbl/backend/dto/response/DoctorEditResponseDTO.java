package com.pbl.backend.dto.response;

import com.pbl.backend.model.Doctor;
import lombok.Data;

@Data
public class DoctorEditResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String position;
    private String degree;
    private String specialty;

    public static DoctorEditResponseDTO fromEntity(Doctor doctor) {
        DoctorEditResponseDTO dto = new DoctorEditResponseDTO();
        dto.setUserId(doctor.getUserId());
        dto.setName(doctor.getName());
        dto.setEmail(doctor.getEmail());
        dto.setPhoneNumber(doctor.getPhoneNumber());
        dto.setPosition(doctor.getPosition());
        dto.setDegree(doctor.getDegree());
        dto.setSpecialty(doctor.getSpecialty());
        return dto;
    }
}
