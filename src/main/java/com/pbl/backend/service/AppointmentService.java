package com.pbl.backend.service;

import com.pbl.backend.dto.AppointmentRequestDTO;
import com.pbl.backend.model.*;
import com.pbl.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

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
        // <<< THAY ĐỔI: Truyền thêm specialtyId vào hàm
        Doctor selectedDoctor = findBestAvailableDoctor(request.getTime(), request.getSpecialtyId());

        // === BƯỚC 3: TẠO VÀ LƯU LỊCH HẸN MỚI ===
        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(savedPatient);
        newAppointment.setDoctor(selectedDoctor);
        newAppointment.setTime(request.getTime());
        newAppointment.setNote(request.getNote());

        return appointmentRepository.save(newAppointment);
    }

    // <<< THAY ĐỔI: Thêm tham số specialtyId
    private Doctor findBestAvailableDoctor(LocalDateTime requestedTime, Integer specialtyId) {
        LocalDate requestedDate = requestedTime.toLocalDate();
        int requestedHour = requestedTime.getHour();

        // <<< THÊM MỚI: Chuyển đổi specialtyId thành tên chuyên khoa dạng String
        String specialtyName;
        if (specialtyId == null) {
            throw new IllegalArgumentException("Chưa chọn chuyên khoa.");
        }
        if (specialtyId == 1) {
            specialtyName = "Khoa thẩm mỹ";
        } else if (specialtyId == 2) {
            specialtyName = "Khoa khám da";
        } else {
            throw new IllegalArgumentException("Chuyên khoa không hợp lệ.");
        }

        // 1. Xác định ca làm việc (AM/PM)
        Schedule.WorkShift shift = getWorkShift(requestedHour);
        if (shift == null) {
            throw new RuntimeException("Giờ khám không hợp lệ, nằm ngoài giờ làm việc.");
        }

        // 2. Tìm tất cả bác sĩ làm việc trong ca đó VÀ ĐÚNG CHUYÊN KHOA
        // <<< THAY ĐỔI: Gọi phương thức repository mới
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

        // Các bước còn lại giữ nguyên logic
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

    // Helper method để xác định ca làm việc (giữ nguyên)
    private Schedule.WorkShift getWorkShift(int hour) {
        if (hour >= 7 && hour < 11) {
            return Schedule.WorkShift.AM;
        }
        if (hour >= 13 && hour < 17) {
            return Schedule.WorkShift.PM;
        }
        return null;
    }

    // Helper methods để lấy giờ bắt đầu/kết thúc ca (giữ nguyên)
    private LocalDateTime getShiftStartTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(7, 0) : date.atTime(13, 0);
    }

    private LocalDateTime getShiftEndTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(11, 0) : date.atTime(17, 0);
    }
}