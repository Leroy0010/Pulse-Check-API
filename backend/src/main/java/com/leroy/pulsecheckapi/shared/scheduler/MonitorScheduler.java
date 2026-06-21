package com.leroy.pulsecheckapi.shared.scheduler;

import com.leroy.pulsecheckapi.module.monitor.model.Monitor;
import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import com.leroy.pulsecheckapi.module.monitor.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorScheduler {
    private final MonitorRepository monitorRepository;

    @Scheduled(fixedRateString = "${app.scheduler.sweep-rate-ms:5000}")
    @Transactional
    public void sweepExpiredMonitors() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // 1. Handle ACTIVE devices that missed their initial heartbeat window
        List<Monitor> missedHeartbeats = monitorRepository.findByStatusAndNextExpectedHeartbeatBefore(
                MonitorStatus.ACTIVE, now
        );
        for (Monitor monitor : missedHeartbeats) {
            monitor.markUnreachable();
            monitorRepository.save(monitor);
            log.warn("{\"WARN\": \"Device {} missed heartbeat window. Entering grace period.\", \"time\": \"{}\"}",
                    monitor.getId(), now);
        }

        // 2. Handle UNREACHABLE devices that also exhausted their grace period
        List<Monitor> deadMonitors = monitorRepository.findByStatusAndGraceExpiresAtBefore(
                MonitorStatus.UNREACHABLE, now
        );
        for (Monitor monitor : deadMonitors) {
            log.error("{\"ALERT\": \"Device {} is officially DOWN! Grace period exhausted.\", \"time\": \"{}\", \"email\": \"{}\"}",
                    monitor.getId(), now, monitor.getAlertEmail());

            monitor.sweepToDown();
            monitorRepository.save(monitor);
        }
    }
}