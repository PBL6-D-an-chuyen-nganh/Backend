package com.pbl.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Getter
@Setter

public class PatientDTO {
    private String name;

    @Email(message = "Email must be in a valid format (e.g., user@example.com)")
    @NotNull(message = "Email cannot be null")
    private String email;

    @Pattern(regexp = "^(0|\\+84)(\\d{9,10})$", message = "Invalid phone number format. Must be 10 or 11 digits starting with 0 or +84.")
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
}
