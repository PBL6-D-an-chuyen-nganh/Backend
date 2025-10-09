package com.pbl.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter

public class AppointmentRequestDTO {
    private PatientDTO patientInfo;
    private LocalDateTime time; // FE gửi lên dạng chuẩn ISO: "2025-10-10T07:30:00"
    private String note;
}
