package com.leroy.pulsecheckapi.shared.exception;

import com.leroy.pulsecheckapi.module.monitor.exception.MonitorAlreadyExistsException;
import com.leroy.pulsecheckapi.module.monitor.exception.MonitorNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // --- 400 Bad Request ---

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable() {
        return build(HttpStatus.BAD_REQUEST, "Invalid or malformed request body.", null, ErrorCode.ILLEGAL_ARGUMENT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        return build(HttpStatus.BAD_REQUEST, "Validation failed.", fieldErrors, ErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null, ErrorCode.ILLEGAL_ARGUMENT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletResponse response) {
        if (response.isCommitted()) return null;
        log.warn("Illegal state transition: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null, ErrorCode.INVALID_STATE_TRANSITION);
    }

    // --- 404 Not Found ---

    @ExceptionHandler(MonitorNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(MonitorNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null, ErrorCode.RESOURCE_NOT_FOUND);
    }

    // --- 409 Conflict ---

    @ExceptionHandler(MonitorAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleConflict(MonitorAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null, ErrorCode.MONITOR_ALREADY_EXISTS);
    }

    // --- 500 Internal Server Error ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletResponse response) {
        if (response.isCommitted()) {
            log.warn("Response already committed. Suppressing: {}", ex.getMessage());
            return null;
        }
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", null, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           Map<String, List<String>> details, ErrorCode code) {
        return ResponseEntity.status(status).body(new ApiError(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, details, code
        ));
    }
}