package com.pbl.backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppointmentStatsDTO {
    private Integer month;
    private Integer year;
    private Long count;

    public AppointmentStatsDTO(Integer month, Integer year, Long count) {
        this.month = month;
        this.year = year;
        this.count = count;
    }
}