package com.pbl.backend.dto.response;

import lombok.Data;

@Data
public class AppointmentInfoForDiagDTO {
    private Long appointmentID;
    private DoctorSummaryDTO doctor;
    private PatientSummaryDTO patientInfo;
}
