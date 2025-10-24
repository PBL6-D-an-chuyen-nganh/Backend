package com.pbl.backend.service;

import com.pbl.backend.dto.ScheduleRequestDTO;
import com.pbl.backend.dto.WorkDaySelectionDTO;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Schedule;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public void createOrUpdateSchedule(ScheduleRequestDTO request) {
        Long doctorId = request.getDoctorId();

        Doctor doctor = doctorRepository.findByUserId(doctorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + doctorId));

        List<LocalDate> datesToUpdate = request.getSelections().stream()
                .map(WorkDaySelectionDTO::getWorkDate)
                .collect(Collectors.toList());


        if (!datesToUpdate.isEmpty()) {
            scheduleRepository.deleteByDoctor_UserIdAndWorkDateIn(doctorId, datesToUpdate);
        }

        List<Schedule> newSchedules = new ArrayList<>();

        for (WorkDaySelectionDTO selection : request.getSelections()) {
            LocalDate workDate = selection.getWorkDate();

            for (Schedule.WorkShift shift : selection.getShifts()) {
                Schedule newSchedule = new Schedule();
                newSchedule.setDoctor(doctor);
                newSchedule.setWorkDate(workDate);
                newSchedule.setShift(shift);
                newSchedules.add(newSchedule);
            }
        }

        if (!newSchedules.isEmpty()) {
            scheduleRepository.saveAll(newSchedules);
        }
    }
}