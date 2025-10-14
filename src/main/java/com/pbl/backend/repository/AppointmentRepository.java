package com.pbl.backend.repository;

import com.pbl.backend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Tìm các cuộc hẹn của 1 bác sĩ sau một thời điểm nhất định
    List<Appointment> findByDoctorUserIdAndTimeAfter(Long doctorId, LocalDateTime time);

    // Tìm cuộc hẹn của bác sĩ tại một thời điểm cụ thể để kiểm tra trùng lặp
    Optional<Appointment> findByDoctorUserIdAndTime(Long doctorId, LocalDateTime time);
}