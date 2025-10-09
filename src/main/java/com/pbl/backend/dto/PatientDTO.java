package com.pbl.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Getter
@Setter

public class PatientDTO {
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
}
