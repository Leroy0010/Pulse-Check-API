package com.leroy.pulsecheckapi.module.monitor.exception;

public class MonitorAlreadyExistsException extends RuntimeException {
    public MonitorAlreadyExistsException(String id) {
        super("A Monitor with ID '" + id + "' already exists.");
    }
}