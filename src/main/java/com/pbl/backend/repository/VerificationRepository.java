package com.pbl.backend.repository;

import com.pbl.backend.model.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findByEmail(String email);
    Optional<Verification> findTopByEmailOrderByExpiredAtDesc(String email);
}


