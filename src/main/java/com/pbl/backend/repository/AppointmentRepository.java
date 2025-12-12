package com.pbl.backend.repository;

import com.pbl.backend.dto.response.AppointmentStatsDTO;
import com.pbl.backend.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorUserIdAndTimeAfter(Long doctorId, LocalDateTime time);
    Optional<Appointment> findByDoctorUserIdAndTime(Long doctorId, LocalDateTime time);
    List<Appointment> findByCreatorId(Long creatorId);
    List<Appointment> findByDoctor_UserIdAndTimeBetween(Long doctorId, LocalDateTime startTime, LocalDateTime endTime);
    List<Appointment> findByStatusAndTimeBefore(String status, LocalDateTime time);
    Page<Appointment> findByCreatorId(Long creatorId, Pageable pageable);
    Page<Appointment> findByDoctor_UserIdAndTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    @Query("SELECT new com.pbl.backend.dto.response.AppointmentStatsDTO(" +
            "   MONTH(a.time), " +
            "   YEAR(a.time), " +
            "   COUNT(a) " +
            ") " +
            "FROM Appointment a " +
            "WHERE a.time >= :startDate AND a.time <= :endDate " +
            "GROUP BY YEAR(a.time), MONTH(a.time) " +
            "ORDER BY YEAR(a.time), MONTH(a.time)")
    List<AppointmentStatsDTO> countAppointmentsByMonth(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}