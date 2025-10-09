package com.pbl.backend.repository;

import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Tìm các bác sĩ đã có lịch hẹn vào một thời điểm chính xác
    @Query("SELECT a.doctor FROM Appointment a WHERE a.time = :exactTime")
    List<Doctor> findDoctorsWithAppointmentAtTime(@Param("exactTime") LocalDateTime exactTime);

    // Đếm số lượng cuộc hẹn của một danh sách bác sĩ trong một khoảng thời gian (ca làm việc)
    @Query("SELECT a.doctor.userId, COUNT(a) FROM Appointment a " +
            "WHERE a.doctor.userId IN :doctorIds AND a.time >= :startTime AND a.time < :endTime " +
            "GROUP BY a.doctor.userId")
    List<Object[]> countAppointmentsForDoctorsInShift(@Param("doctorIds") List<Long> doctorIds,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}