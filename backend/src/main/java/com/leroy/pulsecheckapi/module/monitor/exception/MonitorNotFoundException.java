package com.leroy.pulsecheckapi.module.monitor.exception;

public class MonitorNotFoundException extends RuntimeException {
    public MonitorNotFoundException(String id) {
        super("Monitor with ID '" + id + "' was not found.");
    }
}