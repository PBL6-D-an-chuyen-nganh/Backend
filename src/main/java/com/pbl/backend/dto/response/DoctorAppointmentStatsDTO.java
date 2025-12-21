package com.pbl.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorAppointmentStatsDTO {
    private Long doctorId;
    private String doctorName;
    private Long appointmentCount;
}