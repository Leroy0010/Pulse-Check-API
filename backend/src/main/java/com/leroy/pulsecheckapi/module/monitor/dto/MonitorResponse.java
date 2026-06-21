package com.leroy.pulsecheckapi.module.monitor.dto;

import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class MonitorResponse {
    private String id;
    private Short timeout;
    private String alertEmail;
    private MonitorStatus status;
    private OffsetDateTime expiresAt;
    private Long remainingSeconds;
}