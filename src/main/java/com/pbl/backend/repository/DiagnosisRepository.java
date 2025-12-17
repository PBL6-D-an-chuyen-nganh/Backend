package com.pbl.backend.repository;

import com.pbl.backend.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    @Query("SELECT d FROM Diagnosis d " +
            "LEFT JOIN FETCH d.medicalRecord mr " +
            "LEFT JOIN FETCH mr.patient p " +
            "WHERE d.doctor.userId = :doctorUserId")
    List<Diagnosis> findAllByDoctorUserIdWithDetails(@Param("doctorUserId") Long doctorUserId);

    List<Diagnosis> findByDoctorUserIdAndDateOfDiagnosis(Long doctorUserId, LocalDate dateOfDiagnosis);

    @Query("SELECT d FROM Diagnosis d " +
            "JOIN FETCH d.appointment a " +
            "JOIN FETCH a.patient p " +
            "WHERE p.user.userId = :userId")
    List<Diagnosis> findAllDiagnosesByManagerUserId(@Param("userId") Long userId);
}
