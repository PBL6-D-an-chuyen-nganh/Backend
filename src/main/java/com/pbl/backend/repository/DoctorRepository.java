package com.pbl.backend.repository;

import com.pbl.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    @Query("SELECT d FROM Doctor d WHERE d.specialty = :specialty AND d.authStatus = 'ACTIVE'")
    List<Doctor> findActiveBySpecialty(@Param("specialty") String specialty);

    Optional<Doctor> findByUserId(Long userId);
    boolean existsByEmail(String email);
    Optional<Doctor> findByEmail(String email);
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET auth_status = 'ACTIVE' WHERE user_id = :id", nativeQuery = true)
    void forceReopenUser(@Param("id") Long id);
}