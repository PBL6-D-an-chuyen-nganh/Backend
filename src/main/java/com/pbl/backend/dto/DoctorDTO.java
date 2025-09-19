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
        dto.setIntroduction(doctor.getIntroduction());
        dto.setAvatarFilepath(doctor.getAvatarFilepath());
        dto.setPosition(doctor.getPosition());
        dto.setDegree(doctor.getDegree());
        return dto;
    }
}

