package com.pbl.backend.repository;

import com.pbl.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    List<Doctor> findBySpecialty(String specialty);

    Optional<Doctor> findByUserId(Long userId);
    boolean existsByEmail(String email);
    Optional<Doctor> findByEmail(String email);
}