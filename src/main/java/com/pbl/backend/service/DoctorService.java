package com.pbl.backend.service;

import com.pbl.backend.dto.DoctorDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Schedule;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
                .map(DoctorDTO::fromEntity)  // dùng hàm static fromEntity
                .orElse(null);
    }

    public List<Doctor> getDoctorsBySpecialty(Integer specialtyId) {
        String specialtyName = getSpecialtyNameById(specialtyId);

        return doctorRepository.findBySpecialty(specialtyName);
    }

    public Map<LocalDate, List<LocalTime>> getAvailableSlotsForDoctor(Long doctorId) {
        // Bước 1: Lấy tất cả lịch làm việc và các lịch hẹn đã đặt (giữ nguyên)
        List<Schedule> schedules = scheduleRepository.findByDoctorUserIdAndWorkDateAfter(doctorId, LocalDate.now().minusDays(1));
        List<Appointment> appointments = appointmentRepository.findByDoctorUserIdAndTimeAfter(doctorId, LocalDateTime.now().minusHours(1));

        Set<LocalDateTime> bookedSlots = appointments.stream()
                .map(Appointment::getTime)
                .collect(Collectors.toSet());

        // Bước 2: Tạo ra tất cả các slot có thể có từ lịch làm việc (giữ nguyên)
        Set<LocalDateTime> allPossibleSlots = new HashSet<>();
        for (Schedule schedule : schedules) {
            allPossibleSlots.addAll(generateSlotsForSchedule(schedule));
        }

        // Bước 3: Lọc ra các slot còn trống và trong tương lai (giữ nguyên)
        List<LocalDateTime> availableSlots = allPossibleSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot) && slot.isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        // Bước 4: Chuyển đổi danh sách phẳng thành cấu trúc Map<Ngày, List<Giờ>>
        // Dùng Stream API để nhóm các LocalDateTime theo ngày (toLocalDate),
        // và thu thập các giờ (toLocalTime) vào một danh sách.
        // Sử dụng TreeMap để đảm bảo các ngày được sắp xếp theo thứ tự thời gian.
        Map<LocalDate, List<LocalTime>> groupedSlots = availableSlots.stream()
                .collect(Collectors.groupingBy(
                        LocalDateTime::toLocalDate,
                        TreeMap::new, // Đảm bảo Map được sắp xếp theo ngày
                        Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())
                ));

        // Sắp xếp các giờ trong mỗi ngày
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
