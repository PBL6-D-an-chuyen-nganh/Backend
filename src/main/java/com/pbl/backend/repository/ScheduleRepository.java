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

    // Sửa lại Query để thêm điều kiện lọc theo chuyên khoa của bác sĩ
    @Query("SELECT s.doctor FROM Schedule s WHERE s.workDate = :date AND s.shift = :shift AND s.doctor.specialty = :specialty")
    List<Doctor> findDoctorsByWorkDateAndShiftAndSpecialty(LocalDate date, Schedule.WorkShift shift, String specialty);

    // trong interface ScheduleRepository

    @Query("SELECT s FROM Schedule s JOIN s.doctor d WHERE d.specialty = :specialty AND s.workDate BETWEEN :startDate AND :endDate")
    List<Schedule> findSchedulesBySpecialtyAndDateRange(String specialty, LocalDate startDate, LocalDate endDate);
}