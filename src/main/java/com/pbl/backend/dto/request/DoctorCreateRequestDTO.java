package com.pbl.backend.dto.request;

import lombok.Data;

@Data
public class DoctorCreateRequestDTO {
    private String name;
    private String password;
    private String email;
    private String position;
    private String specialty;
}
