package com.pbl.backend.repository;

import com.pbl.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Doctor findByUserId(Long userId);
}
