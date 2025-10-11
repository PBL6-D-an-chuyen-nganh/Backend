package com.pbl.backend.service;

import com.pbl.backend.dto.AppointmentRequestDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Patient;
import com.pbl.backend.model.Schedule;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.PatientRepository;
import com.pbl.backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    // Giữ nguyên không thay đổi
    @Transactional
    public Appointment createAppointment(AppointmentRequestDTO request) {
        // === BƯỚC 1: TẠO VÀ LƯU BỆNH NHÂN MỚI ===
        Patient newPatient = new Patient();
        newPatient.setName(request.getPatientInfo().getName());
        newPatient.setEmail(request.getPatientInfo().getEmail());
        newPatient.setPhoneNumber(request.getPatientInfo().getPhoneNumber());
        newPatient.setGender(request.getPatientInfo().getGender());
        newPatient.setDateOfBirth(request.getPatientInfo().getDateOfBirth());
        Patient savedPatient = patientRepository.save(newPatient);

        // === BƯỚC 2: CHỌN BÁC SĨ PHÙ HỢP ===
        Doctor selectedDoctor = findBestAvailableDoctor(request.getTime(), request.getSpecialtyId());

        // === BƯỚC 3: TẠO VÀ LƯU LỊCH HẸN MỚI ===
        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(savedPatient);
        newAppointment.setDoctor(selectedDoctor);
        newAppointment.setTime(request.getTime());
        newAppointment.setNote(request.getNote());

        return appointmentRepository.save(newAppointment);
    }

    // Giữ nguyên không thay đổi
    private Doctor findBestAvailableDoctor(LocalDateTime requestedTime, Integer specialtyId) {
        LocalDate requestedDate = requestedTime.toLocalDate();
        int requestedHour = requestedTime.getHour();

        String specialtyName = getSpecialtyNameById(specialtyId);

        // 1. Xác định ca làm việc (AM/PM)
        Schedule.WorkShift shift = getWorkShift(requestedHour);
        if (shift == null) {
            throw new RuntimeException("Giờ khám không hợp lệ, nằm ngoài giờ làm việc.");
        }

        // 2. Tìm tất cả bác sĩ làm việc trong ca đó VÀ ĐÚNG CHUYÊN KHOA
        List<Doctor> workingDoctors = scheduleRepository.findDoctorsByWorkDateAndShiftAndSpecialty(requestedDate, shift, specialtyName);
        if (workingDoctors.isEmpty()) {
            throw new RuntimeException("Không có bác sĩ thuộc chuyên khoa '" + specialtyName + "' làm việc trong ca này.");
        }

        // 3. Loại bỏ các bác sĩ đã có lịch hẹn vào đúng thời điểm đó
        List<Doctor> busyDoctors = appointmentRepository.findDoctorsWithAppointmentAtTime(requestedTime);
        List<Doctor> availableDoctors = workingDoctors.stream()
                .filter(doctor -> !busyDoctors.contains(doctor))
                .collect(Collectors.toList());

        if (availableDoctors.isEmpty()) {
            throw new RuntimeException("Tất cả bác sĩ thuộc chuyên khoa này trong ca đã có lịch vào giờ này.");
        }

        if (availableDoctors.size() == 1) {
            return availableDoctors.get(0);
        }

        // 4. Tìm bác sĩ có ít cuộc hẹn nhất trong ca
        LocalDateTime shiftStartTime = getShiftStartTime(requestedDate, shift);
        LocalDateTime shiftEndTime = getShiftEndTime(requestedDate, shift);

        List<Long> availableDoctorIds = availableDoctors.stream().map(Doctor::getUserId).collect(Collectors.toList());

        List<Object[]> appointmentCounts = appointmentRepository
                .countAppointmentsForDoctorsInShift(availableDoctorIds, shiftStartTime, shiftEndTime);

        Map<Long, Long> doctorAppointmentCountMap = appointmentCounts.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));

        long minAppointments = Long.MAX_VALUE;
        for (Doctor doctor : availableDoctors) {
            long count = doctorAppointmentCountMap.getOrDefault(doctor.getUserId(), 0L);
            if (count < minAppointments) {
                minAppointments = count;
            }
        }

        final long finalMinAppointments = minAppointments;
        List<Doctor> leastBusyDoctors = availableDoctors.stream()
                .filter(doctor -> doctorAppointmentCountMap.getOrDefault(doctor.getUserId(), 0L) == finalMinAppointments)
                .collect(Collectors.toList());

        // 5. Nếu có nhiều bác sĩ thỏa mãn, chọn bác sĩ có ID nhỏ nhất
        return leastBusyDoctors.stream()
                .min(Comparator.comparing(Doctor::getUserId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ phù hợp."));
    }

    // *** BẮT ĐẦU PHẦN THAY ĐỔI ***

    /**
     * Phương thức mới để tạo các slot khám bệnh cho một lịch làm việc cụ thể.
     * Mỗi slot kéo dài 30 phút và có 5 phút nghỉ, do đó thời gian bắt đầu của các slot cách nhau 35 phút.
     *
     * @param schedule Lịch làm việc của bác sĩ.
     * @return Danh sách các LocalDateTime là thời điểm bắt đầu của các slot.
     */
    private List<LocalDateTime> generateSlotsForSchedule(Schedule schedule) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate workDate = schedule.getWorkDate();
        LocalDateTime shiftStartTime = getShiftStartTime(workDate, schedule.getShift());
        LocalDateTime shiftEndTime = getShiftEndTime(workDate, schedule.getShift());

        LocalDateTime currentSlotStart = shiftStartTime;

        // Vòng lặp sẽ tiếp tục khi thời điểm bắt đầu của slot hiện tại
        // cộng thêm 30 phút (thời gian khám) không vượt quá giờ kết thúc ca.
        while (currentSlotStart.plusMinutes(30).isBefore(shiftEndTime) || currentSlotStart.plusMinutes(30).isEqual(shiftEndTime)) {
            slots.add(currentSlotStart);
            // Tăng thời gian lên 35 phút để có 5 phút nghỉ giữa các ca
            currentSlotStart = currentSlotStart.plusMinutes(35);
        }
        return slots;
    }

    // Đã cập nhật để sử dụng phương thức generateSlotsForSchedule
    public List<LocalDate> findUnavailableDatesInMonth(Integer specialtyId) {
        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = YearMonth.from(today).atEndOfMonth();

        if (today.isAfter(endOfMonth)) {
            return Collections.emptyList();
        }

        String specialtyName = getSpecialtyNameById(specialtyId);
        List<Schedule> allSchedules = scheduleRepository.findSchedulesBySpecialtyAndDateRange(specialtyName, today, endOfMonth);

        Map<LocalDate, Set<LocalDateTime>> allPossibleSlotsByDay = new HashMap<>();
        for (Schedule schedule : allSchedules) {
            LocalDate workDate = schedule.getWorkDate();
            allPossibleSlotsByDay.putIfAbsent(workDate, new HashSet<>());
            // Sử dụng phương thức mới để tạo slot
            List<LocalDateTime> slotsForSchedule = generateSlotsForSchedule(schedule);
            allPossibleSlotsByDay.get(workDate).addAll(slotsForSchedule);
        }

        List<LocalDateTime> unavailableSlots = findUnavailableSlots(today, endOfMonth, specialtyId);
        Set<LocalDateTime> unavailableSlotsSet = new HashSet<>(unavailableSlots);

        List<LocalDate> fullyBookedDates = new ArrayList<>();
        for (Map.Entry<LocalDate, Set<LocalDateTime>> entry : allPossibleSlotsByDay.entrySet()) {
            LocalDate date = entry.getKey();
            Set<LocalDateTime> possibleSlots = entry.getValue();
            if (!possibleSlots.isEmpty() && unavailableSlotsSet.containsAll(possibleSlots)) {
                fullyBookedDates.add(date);
            }
        }
        return fullyBookedDates;
    }

    // Giữ nguyên không thay đổi
    public Map<Integer, List<LocalDateTime>> findUnavailableSlotsBySpecialtyForDate(LocalDate date) {
        Map<Integer, List<LocalDateTime>> result = new HashMap<>();
        Map<Integer, String> specialtiesToCheck = Map.of(
                1, "Khoa thẩm mỹ",
                2, "Khoa khám da"
        );
        for (Integer specialtyId : specialtiesToCheck.keySet()) {
            List<LocalDateTime> unavailableSlots = findUnavailableSlots(date, date, specialtyId);
            result.put(specialtyId, unavailableSlots);
        }
        return result;
    }

    // Đã cập nhật để sử dụng phương thức generateSlotsForSchedule
    public List<LocalDateTime> findUnavailableSlots(LocalDate startDate, LocalDate endDate, Integer specialtyId) {
        String specialtyName = getSpecialtyNameById(specialtyId);
        List<LocalDateTime> unavailableSlots = new ArrayList<>();
        List<Schedule> allSchedules = scheduleRepository.findSchedulesBySpecialtyAndDateRange(specialtyName, startDate, endDate);

        // Tính toán tổng số "chỗ" có sẵn cho mỗi slot thời gian
        Map<LocalDateTime, Integer> slotCapacity = new HashMap<>();
        for (Schedule schedule : allSchedules) {
            // Sử dụng phương thức mới để tạo slot
            List<LocalDateTime> slotsForSchedule = generateSlotsForSchedule(schedule);
            for (LocalDateTime slotDateTime : slotsForSchedule) {
                slotCapacity.put(slotDateTime, slotCapacity.getOrDefault(slotDateTime, 0) + 1);
            }
        }

        if (slotCapacity.isEmpty()) {
            return unavailableSlots;
        }

        // Lấy tất cả các cuộc hẹn đã được đặt
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<Appointment> allAppointments = appointmentRepository.findAppointmentsBySpecialtyAndDateTimeRange(specialtyName, startDateTime, endDateTime);
        Map<LocalDateTime, Long> bookedCounts = allAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getTime, Collectors.counting()));

        // So sánh số chỗ có sẵn với số chỗ đã đặt
        for (Map.Entry<LocalDateTime, Integer> entry : slotCapacity.entrySet()) {
            LocalDateTime slot = entry.getKey();
            Integer capacity = entry.getValue();
            Long booked = bookedCounts.getOrDefault(slot, 0L);
            if (booked >= capacity) {
                unavailableSlots.add(slot);
            }
        }
        return unavailableSlots;
    }

    // *** KẾT THÚC PHẦN THAY ĐỔI ***

    // Helper methods giữ nguyên không thay đổi
    private Schedule.WorkShift getWorkShift(int hour) {
        if (hour >= 7 && hour < 11) {
            return Schedule.WorkShift.AM;
        }
        if (hour >= 13 && hour < 17) {
            return Schedule.WorkShift.PM;
        }
        return null;
    }

    private LocalDateTime getShiftStartTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(7, 0) : date.atTime(13, 0);
    }

    private LocalDateTime getShiftEndTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(11, 0) : date.atTime(17, 0);
    }

    private String getSpecialtyNameById(Integer specialtyId) {
        if (specialtyId == null) {
            throw new IllegalArgumentException("Chưa chọn chuyên khoa.");
        }
        switch (specialtyId) {
            case 1:
                return "Khoa thẩm mỹ";
            case 2:
                return "Khoa khám da";
            default:
                throw new IllegalArgumentException("Chuyên khoa không hợp lệ.");
        }
    }
}