package com.leroy.pulsecheckapi.module.monitor.dto;

import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitorActionResponse {
    private String id;
    private String message;
    private MonitorStatus status;
    private OffsetDateTime expiresAt;
    private Long remainingSeconds;
}