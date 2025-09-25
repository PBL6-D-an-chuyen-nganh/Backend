package com.pbl.backend.dto;

import com.pbl.backend.model.Doctor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class DoctorDTO extends UserDTO {
    private String position;
    private String degree;
    private String introduction;
    private String avatarFilepath;

    public static DoctorDTO fromEntity(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setUserId(doctor.getUserId());
        dto.setName(doctor.getName());
        dto.setEmail(doctor.getEmail());
        dto.setPhoneNumber(doctor.getPhoneNumber());
        dto.setIntroduction(doctor.getIntroduction());
        dto.setAvatarFilepath(doctor.getAvatarFilepath());
        dto.setPosition(doctor.getPosition());
        dto.setDegree(doctor.getDegree());
        return dto;
    }
    public Doctor toEntity() {
        Doctor doctor = new Doctor();
        doctor.setUserId(this.getUserId());
        doctor.setName(this.getName());
        doctor.setEmail(this.getEmail());
        doctor.setPhoneNumber(this.getPhoneNumber());
        doctor.setIntroduction(this.getIntroduction());
        doctor.setAvatarFilepath(this.getAvatarFilepath());
        doctor.setPosition(this.getPosition());
        doctor.setDegree(this.getDegree());
        return doctor;
    }
}

