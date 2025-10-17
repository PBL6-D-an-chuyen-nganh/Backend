package com.pbl.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentListResponseDTO {
    private long total;
    private List<AppointmentDetailDTO> appointments;
}