package com.pbl.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ScheduleRequestDTO {
    private Long doctorId;
    private List<WorkDaySelectionDTO> selections;
}