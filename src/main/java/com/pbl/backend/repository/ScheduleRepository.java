package com.pbl.backend.repository;

import com.pbl.backend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // Tìm lịch làm việc của 1 bác sĩ sau một ngày nhất định
    List<Schedule> findByDoctorUserIdAndWorkDateAfter(Long doctorId, LocalDate date);
}