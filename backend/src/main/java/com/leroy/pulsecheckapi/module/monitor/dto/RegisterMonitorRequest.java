package com.leroy.pulsecheckapi.module.monitor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterMonitorRequest {
    @NotBlank(message = "Monitor ID is required")
    @Size(max = 120, message = "Monitor ID must be less than 120 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$", message = "Monitor ID contains invalid characters")
    private String id;

    @NotNull(message = "Timeout value is required")
    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 86400, message = "Timeout cannot exceed 24 hours (86400 seconds)")
    private Short timeout;

    @Min(value = 5, message = "Grace period must be at least 5 seconds")
    @Max(value = 60, message = "Grace period cannot exceed 1 minute (60 seconds)")
    private Short gracePeriod;

    @NotBlank(message = "Alert email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must be less than 150 characters")
    private String alertEmail;
}