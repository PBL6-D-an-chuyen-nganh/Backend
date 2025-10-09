package com.pbl.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {
    private PatientDTO patientInfo;
    private LocalDateTime time;
    private String note;
    private Integer specialtyId; // <<< THÊM TRƯỜNG NÀY (sẽ nhận giá trị 1 hoặc 2)
}