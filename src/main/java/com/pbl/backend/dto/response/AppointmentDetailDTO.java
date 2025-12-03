package com.pbl.backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDetailDTO {
    private Long appointmentID;
    private DoctorSummaryDTO doctor;
    private PatientSummaryDTO patientInfo;
    private LocalDateTime time;
    private String status;
    private String note;
    private Long creatorId;
    private LocalDateTime createdAt;
    private boolean hasDiagnosis;
    private Long diagnosisId;
}