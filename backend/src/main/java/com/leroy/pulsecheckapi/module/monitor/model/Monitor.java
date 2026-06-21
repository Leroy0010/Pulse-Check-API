package com.leroy.pulsecheckapi.module.monitor.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Table(name = "monitors")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // --- Domain Business Logic ---

    /**
     * Initializes a new Monitor safely.
     */
    public Monitor(String id, Short timeout, String alertEmail, OffsetDateTime now) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Monitor ID cannot be empty");
        }
        if (timeout < 5 || timeout > 86400) {
            throw new IllegalArgumentException("Timeout must be between 5 and 86400 seconds");
        }
        if (alertEmail == null || !alertEmail.contains("@")) {
            throw new IllegalArgumentException("A valid alert email is required");
        }
        this.id = id;
        this.timeout = timeout;
        this.alertEmail = alertEmail;
        this.status = MonitorStatus.ACTIVE;
        this.nextExpectedHeartbeat = now.plusSeconds(timeout);
    }

    /**
     * Processes a heartbeat, resetting the countdown window.
     */
    public void processHeartbeat(OffsetDateTime now) {
        this.status = MonitorStatus.ACTIVE;
        this.nextExpectedHeartbeat = now.plusSeconds(this.timeout);
    }

    /**
     * Pauses the monitor, clearing the countdown.
     */
    public void pause() {
        if (this.status == MonitorStatus.DOWN) {
            throw new IllegalStateException("Cannot pause a monitor that is DOWN");
        }
        this.status = MonitorStatus.PAUSED;
        this.nextExpectedHeartbeat = null;
    }

    /**
     * Moves a DOWN monitor into the recovery state.
     */
    public void initiateRecovery() {
        if (this.status != MonitorStatus.DOWN) {
            throw new IllegalStateException("Monitor must be DOWN to enter recovery status");
        }
        this.status = MonitorStatus.UNDER_RECOVERY;
        this.nextExpectedHeartbeat = null;
    }

    /**
     * Brings a recovering monitor back online.
     */
    public void completeRecovery(OffsetDateTime now) {
        if (this.status != MonitorStatus.UNDER_RECOVERY) {
            throw new IllegalStateException("Monitor is not currently marked UNDER_RECOVERY");
        }
        this.status = MonitorStatus.ACTIVE;
        this.nextExpectedHeartbeat = now.plusSeconds(this.timeout);
    }

    public void sweep(){
        this.status = MonitorStatus.DOWN;
        this.nextExpectedHeartbeat = null;

    }
}