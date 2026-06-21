package com.leroy.pulsecheckapi.module.monitor.repository;

import com.leroy.pulsecheckapi.module.monitor.model.Monitor;
import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, String> {
    // Highly efficient targeted query matching our partial conditional index
    List<Monitor> findByStatusAndNextExpectedHeartbeatBefore(MonitorStatus status, OffsetDateTime currentTime);
}