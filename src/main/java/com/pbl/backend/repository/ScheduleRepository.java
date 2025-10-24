package com.pbl.backend.repository;

import com.pbl.backend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDoctorUserIdAndWorkDateAfter(Long doctorId, LocalDate date);
    void deleteByDoctor_UserIdAndWorkDateIn(Long doctorId, List<LocalDate> dates);
}