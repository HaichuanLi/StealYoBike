package com.acme.bms.api.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.acme.bms.application.exception.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.hibernate.LazyInitializationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrors.class);

    // ---------- Validation ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.<String, Object>of("field", f.getField(), "message", f.getDefaultMessage()))
                .toList();

        return problem(400, "Validation failed", "Please fix the highlighted fields.",
                "https://api.bms/errors/validation",
                Map.of("errors", errors));
    }

    // Auth/Id
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> unauthorized(InvalidCredentialsException ex) {
        return problem(401, "Unauthorized", ex.getMessage(),
                "https://api.bms/errors/unauthorized", Map.of());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> userNotFound(UserNotFoundException ex) {
        return problem(401, "Unauthorized", ex.getMessage(),
                "https://api.bms/errors/unauthorized", Map.of());
    }

    @ExceptionHandler(OperatorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> operatorNotFound(OperatorNotFoundException ex) {
        return problem(404, "Not found", ex.getMessage(),
                "https://api.bms/errors/not-found", Map.of());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, Object>> forbidden(ForbiddenOperationException ex) {
        return problem(403, "Forbidden", ex.getMessage(),
                "https://api.bms/errors/forbidden", Map.of());
    }

    // Registration conflicts
    @ExceptionHandler({ EmailAlreadyUsedException.class, UsernameAlreadyUsedException.class })
    public ResponseEntity<Map<String, Object>> conflict(RuntimeException ex) {
        return problem(409, "Conflict", ex.getMessage(),
                "https://api.bms/errors/conflict", Map.of());
    }

    // Station / Trip / Docks
    @ExceptionHandler(StationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> stationNotFound(StationNotFoundException ex) {
        return problem(404, "Not found", ex.getMessage(),
                "https://api.bms/errors/not-found", Map.of());
    }

    @ExceptionHandler(TripNotFoundException.class)
    public ResponseEntity<Map<String, Object>> tripNotFound(TripNotFoundException ex) {
        return problem(404, "Not found", ex.getMessage(),
                "https://api.bms/errors/not-found", Map.of());
    }

    @ExceptionHandler(StationAlreadyOutOfServiceException.class)
    public ResponseEntity<Map<String, Object>> alreadyOos(StationAlreadyOutOfServiceException ex) {
        return problem(409, "Conflict", ex.getMessage(),
                "https://api.bms/errors/conflict", Map.of());
    }

    @ExceptionHandler(NoAvailableBikesException.class)
    public ResponseEntity<Map<String, Object>> noBikes(NoAvailableBikesException ex) {
        return problem(422, "No bikes available", ex.getMessage(),
                "https://api.bms/errors/unprocessable", Map.of());
    }

    @ExceptionHandler(TripNotActiveException.class)
    public ResponseEntity<Map<String, Object>> tripNotActive(TripNotActiveException ex) {
        return problem(422, "Unprocessable Entity", ex.getMessage(),
                "https://api.bms/errors/unprocessable", Map.of());
    }

    @ExceptionHandler(NoEmptyDockAvailableException.class)
    public ResponseEntity<Map<String, Object>> noEmptyDock(NoEmptyDockAvailableException ex) {
        return problem(422, "No empty dock", ex.getMessage(),
                "https://api.bms/errors/unprocessable", Map.of());
    }

    @ExceptionHandler(BikeReturnFailedException.class)
    public ResponseEntity<Map<String, Object>> bikeReturnFailed(BikeReturnFailedException ex) {
        return problem(409, "Return failed", ex.getMessage(),
                "https://api.bms/errors/conflict", Map.of());
    }

    // UC7
    @ExceptionHandler(BikeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> bikeNotFound(BikeNotFoundException ex) {
        return problem(404, "Not found", ex.getMessage(),
                "https://api.bms/errors/not-found", Map.of());
    }

    @ExceptionHandler(BikeMaintenanceStateException.class)
    public ResponseEntity<Map<String, Object>> bikeStateInvalid(BikeMaintenanceStateException ex) {
        return problem(422, "Unprocessable Entity", ex.getMessage(),
                "https://api.bms/errors/unprocessable", Map.of());
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<Map<String, Object>> lazyInitialization(LazyInitializationException ex) {
        logger.error("LazyInitializationException: {}", ex.toString());
        return problem(500, "Data access error",
                "Attempted to access lazily-loaded data outside a Hibernate session. Consider using DTOs, fetch joins, or ensuring the data is loaded within a transaction.",
                "https://api.bms/errors/lazy-initialization", Map.of());
    }

    @ExceptionHandler(java.io.IOException.class)
    public ResponseEntity<Map<String, Object>> ioException(java.io.IOException ex) {
        // Broken pipe errors are expected when SSE clients disconnect
        // Log at debug level instead of error to reduce noise
        if (ex.getMessage() != null && ex.getMessage().contains("Broken pipe")) {
            logger.debug("SSE client disconnected: {}", ex.getMessage());
            // Return 200 since this is expected behavior
            return ResponseEntity.ok().build();
        }
        // For other IOExceptions, log as error
        logger.error("IOException: {}", ex.toString());
        return problem(500, "Internal Server Error",
                "An I/O error occurred. If the issue persists, contact support.",
                "https://api.bms/errors/internal", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> fallback(Exception ex) {
        logger.error("Unhandled exception: {}", ex.toString());
        return problem(500, "Internal Server Error",
                "Something went wrong. If the issue persists, contact support.",
                "https://api.bms/errors/internal", Map.of());
    }

    // Helper
    private ResponseEntity<Map<String, Object>> problem(
            int status, String title, String detail, String type, Map<String, Object> extra) {

        var body = new LinkedHashMap<String, Object>();
        body.put("type", type);
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        body.put("traceId", UUID.randomUUID().toString());
        extra.forEach(body::put);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
