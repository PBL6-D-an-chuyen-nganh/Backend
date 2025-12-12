package com.pbl.backend.repository;

import com.pbl.backend.dto.response.UserCancellationStatsDTO;
import com.pbl.backend.model.AppointmentCancellationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CancellationLogRepository extends JpaRepository<AppointmentCancellationLog, Long> {

    long countByCancelledBy_UserId(Long userId);

    @Query("SELECT COUNT(c) FROM AppointmentCancellationLog c " +
            "WHERE c.cancelledBy.userId = :userId " +
            "AND c.cancelledAt >= :fromDate")
    long countRecentCancellations(@Param("userId") Long userId,
                                  @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT new com.pbl.backend.dto.response.UserCancellationStatsDTO(" +
            "   l.cancelledBy.userId, " +
            "   l.cancelledBy.name, " +
            "   l.cancelledBy.email, " +
            "   COUNT(l) " +
            ") " +
            "FROM AppointmentCancellationLog l " +
            "GROUP BY l.cancelledBy.userId, l.cancelledBy.name, l.cancelledBy.email " +
            "ORDER BY COUNT(l) DESC")
    List<UserCancellationStatsDTO> getCancellationStatistics();
}