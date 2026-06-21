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
        List<Monitor> deadMonitors = monitorRepository.findByStatusAndNextExpectedHeartbeatBefore(
                MonitorStatus.ACTIVE, now
        );

        for (Monitor monitor : deadMonitors) {
            // Emitting standardized structured log output for alerting infrastructure
            log.error("{\"ALERT\": \"Device {} is down!\", \"time\": \"{}\", \"email\": \"{}\"}",
                    monitor.getId(), now, monitor.getAlertEmail());

            // Atomically switch state down to isolate from successive checks
            monitor.sweep();
            monitorRepository.save(monitor);
        }
    }
}