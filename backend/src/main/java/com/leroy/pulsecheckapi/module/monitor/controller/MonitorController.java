package com.leroy.pulsecheckapi.module.monitor.controller;

import com.leroy.pulsecheckapi.module.monitor.dto.MonitorActionResponse;
import com.leroy.pulsecheckapi.module.monitor.dto.MonitorResponse;
import com.leroy.pulsecheckapi.module.monitor.dto.RegisterMonitorRequest;
import com.leroy.pulsecheckapi.module.monitor.model.MonitorStatus;
import com.leroy.pulsecheckapi.module.monitor.service.MonitorService;
import com.leroy.pulsecheckapi.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/monitors")
@RequiredArgsConstructor
@Tag(name = "Watchdog Monitors Management System")
public class MonitorController {

    private final MonitorService monitorService;

    @PostMapping
    public ResponseEntity<ApiResponse<MonitorActionResponse>> registerMonitor(@Valid @RequestBody RegisterMonitorRequest request) {
        MonitorResponse res = monitorService.createMonitor(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(res.getId())
                .toUri();

        MonitorActionResponse data = MonitorActionResponse.builder()
                .id(res.getId())
                .expiresAt(res.getExpiresAt())
                .build();

        return ResponseEntity.created(location)
                .body(ApiResponse.success("Monitor registered and initialized successfully", data));
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<ApiResponse<MonitorActionResponse>> heartbeat(@PathVariable String id) {
        MonitorResponse res = monitorService.processHeartbeat(id);
        MonitorActionResponse data = MonitorActionResponse.builder()
                .id(res.getId())
                .status(res.getStatus())
                .expiresAt(res.getExpiresAt())
                .remainingSeconds(res.getRemainingSeconds())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Heartbeat received successfully. Watchdog window reset.", data));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<MonitorActionResponse>> pauseMonitor(@PathVariable String id) {
        MonitorResponse res = monitorService.pauseMonitor(id);
        MonitorActionResponse data = MonitorActionResponse.builder()
                .id(res.getId())
                .status(res.getStatus())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Watchdog tracking paused. Countdown suspended.", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MonitorResponse>> getMonitorById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Monitor fetched successfully.", monitorService.getMonitorById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MonitorResponse>>> getAllMonitors(
            @RequestParam( required = false) MonitorStatus status,
            @PageableDefault (sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success("Monitors fetched successfully.", monitorService.getAllMonitors(status, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMonitor(@PathVariable String id) {
        monitorService.deleteMonitor(id);
        return ResponseEntity.ok(ApiResponse.success("Monitor un-registered and completely deleted from history."));
    }
}