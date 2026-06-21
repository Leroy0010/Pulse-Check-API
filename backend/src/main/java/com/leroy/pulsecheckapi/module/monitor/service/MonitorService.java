package com.leroy.pulsecheckapi.module.monitor.service;

import com.leroy.pulsecheckapi.module.monitor.dto.MonitorResponse;
import com.leroy.pulsecheckapi.module.monitor.dto.RegisterMonitorRequest;
import com.leroy.pulsecheckapi.module.monitor.exception.MonitorAlreadyExistsException;
import com.leroy.pulsecheckapi.module.monitor.exception.MonitorNotFoundException;
import com.leroy.pulsecheckapi.module.monitor.model.Monitor;
import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import com.leroy.pulsecheckapi.module.monitor.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

    private final MonitorRepository monitorRepository;

    @Transactional
    public MonitorResponse createMonitor(RegisterMonitorRequest request) {
        if (monitorRepository.existsById(request.getId())) {
            throw new MonitorAlreadyExistsException(request.getId());
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        var gracePeriod = request.getGracePeriod() != null ? request.getGracePeriod() : 15;

        Monitor monitor = new Monitor(
                request.getId(),
                request.getTimeout(),
                gracePeriod,
                request.getAlertEmail(),
                now
        );

        Monitor saved = monitorRepository.save(monitor);
        return mapToResponse(saved, now);
    }

    @Transactional
    public MonitorResponse processHeartbeat(String id) {
        Monitor monitor = getMonitorOrThrow(id);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        monitor.processHeartbeat(now);
        Monitor updated = monitorRepository.save(monitor);
        return mapToResponse(updated, now);
    }

    @Transactional
    public MonitorResponse pauseMonitor(String id) {
        Monitor monitor = getMonitorOrThrow(id);

        monitor.pause();

        Monitor updated = monitorRepository.save(monitor);
        return mapToResponse(updated, OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional(readOnly = true)
    public MonitorResponse getMonitorById(String id) {
        return mapToResponse(getMonitorOrThrow(id), OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional(readOnly = true)
    public Page<MonitorResponse> getAllMonitors(MonitorStatus status, Pageable pageable) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return monitorRepository.findAllAndStatus(status, pageable)
                .map(monitor -> mapToResponse(monitor, now));
    }

    @Transactional
    public void deleteMonitor(String id) {
        if (!monitorRepository.existsById(id)) {
            throw new MonitorNotFoundException(id);
        }
        monitorRepository.deleteById(id);
    }

    // --- Helper Methods ---

    private Monitor getMonitorOrThrow(String id) {
        return monitorRepository.findById(id)
                .orElseThrow(() -> new MonitorNotFoundException(id));
    }

    private MonitorResponse mapToResponse(Monitor monitor, OffsetDateTime referenceTime) {
        Long remaining = null;

        if (monitor.getStatus() == MonitorStatus.ACTIVE && monitor.getNextExpectedHeartbeat() != null) {
            remaining = Duration.between(referenceTime, monitor.getNextExpectedHeartbeat()).toSeconds();
            if (remaining < 0) remaining = 0L;
        } else if (monitor.getStatus() == MonitorStatus.UNREACHABLE && monitor.getGraceExpiresAt() != null) {
            // Provide real-time countdown for the grace period remaining
            remaining = Duration.between(referenceTime, monitor.getGraceExpiresAt()).toSeconds();
            if (remaining < 0) remaining = 0L;
        }

        return MonitorResponse.builder()
                .id(monitor.getId())
                .timeout(monitor.getTimeout())
                .alertEmail(monitor.getAlertEmail())
                .status(monitor.getStatus())
                .expiresAt(monitor.getStatus() == MonitorStatus.UNREACHABLE ? monitor.getGraceExpiresAt() : monitor.getNextExpectedHeartbeat())
                .remainingSeconds(remaining)
                .build();
    }
}