package com.leroy.pulsecheckapi.module.monitor.controller;

import com.leroy.pulsecheckapi.module.monitor.dto.MonitorActionResponse;
import com.leroy.pulsecheckapi.module.monitor.dto.MonitorResponse;
import com.leroy.pulsecheckapi.module.monitor.dto.RegisterMonitorRequest;
import com.leroy.pulsecheckapi.module.monitor.service.MonitorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/monitors")
@RequiredArgsConstructor
@Tag(name = "Watchdog Monitors Management System")
public class MonitorController {

    private final MonitorService monitorService;

    @PostMapping
    public ResponseEntity<MonitorActionResponse> registerMonitor(@Valid @RequestBody RegisterMonitorRequest request) {
        MonitorResponse res = monitorService.createMonitor(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(res.getId())
                .toUri();
        return ResponseEntity.created(location).body(
                MonitorActionResponse.builder()
                        .id(res.getId())
                        .message("Monitor registered and initialized successfully")
                        .expiresAt(res.getExpiresAt())
                        .build()
        );
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<MonitorActionResponse> heartbeat(@PathVariable String id) {
        MonitorResponse res = monitorService.processHeartbeat(id);
        return ResponseEntity.ok(
                MonitorActionResponse.builder()
                        .id(res.getId())
                        .status(res.getStatus())
                        .message("Heartbeat received successfully. Watchdog window reset.")
                        .expiresAt(res.getExpiresAt())
                        .remainingSeconds(res.getRemainingSeconds())
                        .build()
        );
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<MonitorActionResponse> pauseMonitor(@PathVariable String id) {
        MonitorResponse res = monitorService.pauseMonitor(id);
        return ResponseEntity.ok(
                MonitorActionResponse.builder()
                        .id(res.getId())
                        .status(res.getStatus())
                        .message("Watchdog tracking paused. Countdown suspended.")
                        .build()
        );
    }

    @PostMapping("/{id}/recovery")
    public ResponseEntity<MonitorActionResponse> initiateRecovery(@PathVariable String id) {
        MonitorResponse res = monitorService.initiateRecovery(id);
        return ResponseEntity.ok(
                MonitorActionResponse.builder()
                        .id(res.getId())
                        .status(res.getStatus())
                        .message("Monitor transitioned to recovery state. Alert sweep suspended.")
                        .build()
        );
    }

    @PostMapping("/{id}/recovery/complete")
    public ResponseEntity<MonitorActionResponse> completeRecovery(@PathVariable String id) {
        MonitorResponse res = monitorService.completeRecovery(id);
        return ResponseEntity.ok(
                MonitorActionResponse.builder()
                        .id(res.getId())
                        .status(res.getStatus())
                        .message("Device maintenance complete. System returned to active monitoring loop.")
                        .expiresAt(res.getExpiresAt())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonitorResponse> getMonitorById(@PathVariable String id) {
        return ResponseEntity.ok(monitorService.getMonitorById(id));
    }

    @GetMapping
    public ResponseEntity<List<MonitorResponse>> getAllMonitors() {
        return ResponseEntity.ok(monitorService.getAllMonitors());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MonitorActionResponse> deleteMonitor(@PathVariable String id) {
        monitorService.deleteMonitor(id);
        return ResponseEntity.ok(
                MonitorActionResponse.builder()
                        .id(id)
                        .message("Monitor un-registered and completely deleted from history.")
                        .build()
        );
    }
}