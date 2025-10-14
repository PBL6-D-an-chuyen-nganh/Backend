package com.pbl.backend.service;

import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Schedule;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public List<Doctor> getDoctorsBySpecialty(Integer specialtyId) {
        String specialtyName = getSpecialtyNameById(specialtyId);

        return doctorRepository.findBySpecialty(specialtyName);
    }

    public List<LocalDateTime> getAvailableSlotsForDoctor(Long doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorUserIdAndWorkDateAfter(doctorId, LocalDate.now().minusDays(1));

        List<Appointment> appointments = appointmentRepository.findByDoctorUserIdAndTimeAfter(doctorId, LocalDateTime.now().minusHours(1));
        Set<LocalDateTime> bookedSlots = appointments.stream()
                .map(Appointment::getTime)
                .collect(Collectors.toSet());

        Set<LocalDateTime> allPossibleSlots = new HashSet<>();
        for (Schedule schedule : schedules) {
            allPossibleSlots.addAll(generateSlotsForSchedule(schedule));
        }

        return allPossibleSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot) && slot.isAfter(LocalDateTime.now()))
                .sorted()
                .collect(Collectors.toList());
    }
    private String getSpecialtyNameById(Integer specialtyId) {
        if (specialtyId == null) {
            throw new IllegalArgumentException("Chưa chọn chuyên khoa (specialtyId is null).");
        }
        switch (specialtyId) {
            case 1:
                return "Khoa thẩm mỹ";
            case 2:
                return "Khoa khám da";
            default:
                throw new IllegalArgumentException("Chuyên khoa không hợp lệ (Invalid specialtyId: " + specialtyId + ").");
        }
    }

    private List<LocalDateTime> generateSlotsForSchedule(Schedule schedule) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate workDate = schedule.getWorkDate();
        LocalDateTime shiftStartTime = getShiftStartTime(workDate, schedule.getShift());
        LocalDateTime shiftEndTime = getShiftEndTime(workDate, schedule.getShift());
        LocalDateTime currentSlotStart = shiftStartTime;

        while (currentSlotStart.isBefore(shiftEndTime)) {
            slots.add(currentSlotStart);
            currentSlotStart = currentSlotStart.plusMinutes(35);
        }
        return slots;
    }

    private LocalDateTime getShiftStartTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(7, 0) : date.atTime(13, 0);
    }

    private LocalDateTime getShiftEndTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(11, 0) : date.atTime(17, 0);
    }
}
