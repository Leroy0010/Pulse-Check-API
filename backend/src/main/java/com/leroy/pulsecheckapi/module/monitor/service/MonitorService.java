package com.leroy.pulsecheckapi.module.monitor.service;

import com.leroy.pulsecheckapi.module.monitor.dto.MonitorResponse;
import com.leroy.pulsecheckapi.module.monitor.dto.RegisterMonitorRequest;
import com.leroy.pulsecheckapi.module.monitor.model.Monitor;
import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import com.leroy.pulsecheckapi.module.monitor.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

    private final MonitorRepository monitorRepository;

    @Transactional
    public MonitorResponse createMonitor(RegisterMonitorRequest request) {
        if (monitorRepository.existsById(request.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Monitor ID already exists");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // Use the Domain Entity constructor to ensure safe initialization
        Monitor monitor = new Monitor(
                request.getId(),
                request.getTimeout(),
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

        // Delegate to domain model
        monitor.processHeartbeat(now);

        Monitor updated = monitorRepository.save(monitor);
        return mapToResponse(updated, now);
    }

    @Transactional
    public MonitorResponse pauseMonitor(String id) {
        Monitor monitor = getMonitorOrThrow(id);

        try {
            monitor.pause(); // Delegate to domain model
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        Monitor updated = monitorRepository.save(monitor);
        return mapToResponse(updated, OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional
    public MonitorResponse initiateRecovery(String id) {
        Monitor monitor = getMonitorOrThrow(id);

        try {
            monitor.initiateRecovery(); // Delegate to domain model
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        Monitor updated = monitorRepository.save(monitor);
        var now = OffsetDateTime.now(ZoneOffset.UTC);

        log.info("{{\"RECOVERY_START\": \"Monitor {} is under technician repair\", \"time\": \"{}\"}}", id, now);

        return mapToResponse(updated, now);
    }

    @Transactional
    public MonitorResponse completeRecovery(String id) {
        Monitor monitor = getMonitorOrThrow(id);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        try {
            monitor.completeRecovery(now); // Delegate to domain model
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        Monitor updated = monitorRepository.save(monitor);

        log.info("{{\"RECOVERY_COMPLETE\": \"Monitor {} is back online\", \"time\": \"{}\"}}", id, now);
        return mapToResponse(updated, now);
    }

    @Transactional(readOnly = true)
    public MonitorResponse getMonitorById(String id) {
        return mapToResponse(getMonitorOrThrow(id), OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional(readOnly = true)
    public List<MonitorResponse> getAllMonitors() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return monitorRepository.findAll().stream()
                .map(monitor -> mapToResponse(monitor, now))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMonitor(String id) {
        if (!monitorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found");
        }
        monitorRepository.deleteById(id);
    }

    // --- Helper Methods ---

    private Monitor getMonitorOrThrow(String id) {
        return monitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));
    }

    private MonitorResponse mapToResponse(Monitor monitor, OffsetDateTime referenceTime) {
        Long remaining = null;
        if (monitor.getStatus() == MonitorStatus.ACTIVE && monitor.getNextExpectedHeartbeat() != null) {
            remaining = Duration.between(referenceTime, monitor.getNextExpectedHeartbeat()).toSeconds();
            if (remaining < 0) remaining = 0L;
        }

        return MonitorResponse.builder()
                .id(monitor.getId())
                .timeout(monitor.getTimeout())
                .alertEmail(monitor.getAlertEmail())
                .status(monitor.getStatus())
                .expiresAt(monitor.getNextExpectedHeartbeat())
                .remainingSeconds(remaining)
                .build();
    }
}