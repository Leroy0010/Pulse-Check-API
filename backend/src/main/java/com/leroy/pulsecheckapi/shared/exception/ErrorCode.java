package com.leroy.pulsecheckapi.shared.exception;

public enum ErrorCode {
    // 404
    RESOURCE_NOT_FOUND,

    // 409
    MONITOR_ALREADY_EXISTS,

    // 400
    VALIDATION_FAILED,
    ILLEGAL_ARGUMENT,
    INVALID_STATE_TRANSITION,

    // 500
    INTERNAL_SERVER_ERROR
}