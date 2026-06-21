package com.leroy.pulsecheckapi.module.monitor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Table(name = "monitors")
@Entity
@Getter
@Setter
public class Monitor {
    @Id
    @Column(nullable = false, length = 120)
    private String id;

    @Column(nullable = false, name = "alert_email", length = 150)
    private String alertEmail;

    @Column(name = "timeout", nullable = false)
    private Short timeout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MonitorStatus status;

    @Column(name = "next_expected_heartbeat")
    private OffsetDateTime nextExpectedHeartbeat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}