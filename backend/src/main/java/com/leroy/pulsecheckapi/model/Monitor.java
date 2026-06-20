package com.leroy.pulsecheckapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Table(name = "monitors")
@Entity
@Getter @Setter
public class Monitor {
    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false, name = "alert_email")
    private String alertEmail;

    @Column(name = "timeout",  nullable = false)
    private Short timeout;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
