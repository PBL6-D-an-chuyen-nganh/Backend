package com.pbl.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {
    private PatientDTO patientInfo;
    private LocalDateTime time;
    private String note;
    private Long doctorId;
    private Long creatorId;
}