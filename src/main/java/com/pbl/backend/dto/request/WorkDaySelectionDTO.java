package com.pbl.backend.dto.request;

import com.pbl.backend.model.Schedule; // Import model của bạn
import lombok.Data;
import java.time.LocalDate;
import java.util.List;


@Data
public class WorkDaySelectionDTO {
    private LocalDate workDate;
    private List<Schedule.WorkShift> shifts;
}