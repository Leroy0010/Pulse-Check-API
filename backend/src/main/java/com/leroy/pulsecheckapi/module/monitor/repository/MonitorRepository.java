package com.leroy.pulsecheckapi.module.monitor.repository;

import com.leroy.pulsecheckapi.module.monitor.model.Monitor;
import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, String> {
    // Highly efficient targeted query matching our partial conditional index
    List<Monitor> findByStatusAndNextExpectedHeartbeatBefore(MonitorStatus status, OffsetDateTime currentTime);

    List<Monitor> findByStatusAndGraceExpiresAtBefore(MonitorStatus status, OffsetDateTime graceExpiresAtBefore);

    @Query("SELECT m FROM Monitor m WHERE (CAST(:status AS string) IS NULL OR m.status = :status)")
    Page<Monitor> findAllAndStatus(@Param("status") MonitorStatus status, Pageable pageable);
}