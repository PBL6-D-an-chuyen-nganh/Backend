package com.pbl.backend.repository;

import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s.doctor FROM Schedule s WHERE s.workDate = :date AND s.shift = :shift")
    List<Doctor> findDoctorsByWorkDateAndShift(LocalDate date, Schedule.WorkShift shift);
}
