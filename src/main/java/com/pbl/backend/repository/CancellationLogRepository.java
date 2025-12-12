package com.pbl.backend.repository;

import com.pbl.backend.model.AppointmentCancellationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CancellationLogRepository extends JpaRepository<AppointmentCancellationLog, Long> {

    long countByCancelledBy_UserId(Long userId);

    @Query("SELECT COUNT(c) FROM AppointmentCancellationLog c " +
            "WHERE c.cancelledBy.userId = :userId " +
            "AND c.cancelledAt >= :fromDate")
    long countRecentCancellations(@Param("userId") Long userId,
                                  @Param("fromDate") LocalDateTime fromDate);
}