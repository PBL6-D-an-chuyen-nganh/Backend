package com.pbl.backend.repository;

import com.pbl.backend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorUserIdAndTimeAfter(Long doctorId, LocalDateTime time);

    Optional<Appointment> findByDoctorUserIdAndTime(Long doctorId, LocalDateTime time);
}