package com.pbl.backend.service;

import com.pbl.backend.dto.DoctorDTO;
import com.pbl.backend.dto.DoctorSummaryDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Schedule;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.ScheduleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public Page<DoctorDTO> getDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findAll(pageable);
        return doctors.map(DoctorDTO::fromEntity);
    }

    public DoctorDTO getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .map(DoctorDTO::fromEntity)
                .orElse(null);
    }

    public List<DoctorSummaryDTO> getDoctorsBySpecialty(Integer specialtyId) {
        String specialtyName = getSpecialtyNameById(specialtyId);

        List<Doctor> doctors = doctorRepository.findBySpecialty(specialtyName);

        return doctors.stream()
                .map(DoctorSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<DoctorDTO> searchDoctors(String name, String degree, String position, Pageable pageable) {
        Specification<Doctor> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(degree)) {
                predicates.add(criteriaBuilder.equal(root.get("degree"), degree));
            }

            if (StringUtils.hasText(position)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("position")),
                        "%" + position.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Doctor> doctors = doctorRepository.findAll(spec, pageable);
        return doctors.map(DoctorDTO::fromEntity);
    }

    public Map<LocalDate, List<LocalTime>> getAvailableSlotsForDoctor(Long doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorUserIdAndWorkDateAfter(doctorId, LocalDate.now().minusDays(1));
        List<Appointment> appointments = appointmentRepository.findByDoctorUserIdAndTimeAfter(doctorId, LocalDateTime.now().minusHours(1));

        Set<LocalDateTime> bookedSlots = appointments.stream()
                .map(Appointment::getTime)
                .collect(Collectors.toSet());

        Set<LocalDateTime> allPossibleSlots = new HashSet<>();
        for (Schedule schedule : schedules) {
            allPossibleSlots.addAll(generateSlotsForSchedule(schedule));
        }

        List<LocalDateTime> availableSlots = allPossibleSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot) && slot.isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        Map<LocalDate, List<LocalTime>> groupedSlots = availableSlots.stream()
                .collect(Collectors.groupingBy(
                        LocalDateTime::toLocalDate,
                        TreeMap::new,
                        Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())
                ));

        groupedSlots.forEach((date, times) -> times.sort(LocalTime::compareTo));

        return groupedSlots;
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
