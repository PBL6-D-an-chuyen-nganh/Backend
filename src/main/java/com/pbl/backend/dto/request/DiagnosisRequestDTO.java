package com.pbl.backend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DiagnosisRequestDTO {
    private Long patientId;
    private Long doctorUserId;
    private String disease;
    private LocalDate dateOfDiagnosis;
    private String doctorNotes;
    private String treatmentPlan;
}