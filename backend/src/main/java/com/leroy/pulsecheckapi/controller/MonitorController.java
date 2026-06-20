package com.leroy.pulsecheckapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitors")
@RequiredArgsConstructor
public class MonitorController {
    @GetMapping
    public String getMonitors() {
        return "Monitors: ";
    }
}
