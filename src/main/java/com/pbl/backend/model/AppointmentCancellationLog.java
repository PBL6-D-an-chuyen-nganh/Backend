package com.pbl.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_cancellation_logs", indexes = {
        @Index(name = "idx_user_cancel", columnList = "user_id, cancelled_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCancellationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id_ref")
    private Long appointmentIdRef;

    private LocalDateTime appointmentTime;

    private String doctorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User cancelledBy;

    @CreationTimestamp
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}