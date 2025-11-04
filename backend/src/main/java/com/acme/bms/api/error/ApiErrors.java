package com.acme.bms.api.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.acme.bms.application.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// UC4-related (optional; include if you created these)


@RestControllerAdvice
public class ApiErrors {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.<String, Object>of("field", f.getField(), "message", f.getDefaultMessage()))
                .toList();

        return problem(400, "Validation failed", "Please fix the highlighted fields.",
                "https://api.bms/errors/validation",
                Map.of("errors", errors));
    }

    @ExceptionHandler({ EmailAlreadyUsedException.class, UsernameAlreadyUsedException.class })
    public ResponseEntity<Map<String, Object>> conflict(RuntimeException ex) {
        return problem(409, "Conflict", ex.getMessage(),
                "https://api.bms/errors/conflict", Map.of());
    }

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> fallback(Exception ex) {
        // Avoid leaking stack traces to clients; log server-side instead
        return problem(500, "Internal Server Error",
                "Something went wrong. If the issue persists, contact support.",
                "https://api.bms/errors/internal", Map.of());
    }

    @ExceptionHandler(OperatorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> operatorNotFound(OperatorNotFoundException ex) {
        return problem(404, "Not found", ex.getMessage(), "https://api.bms/errors/not-found", Map.of());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, Object>> forbidden(ForbiddenOperationException ex) {
        return problem(403, "Forbidden", ex.getMessage(), "https://api.bms/errors/forbidden", Map.of());
    }

    @ExceptionHandler(StationAlreadyOutOfServiceException.class)
    public ResponseEntity<Map<String, Object>> alreadyOos(StationAlreadyOutOfServiceException ex) {
        return problem(409, "Conflict", ex.getMessage(), "https://api.bms/errors/conflict", Map.of());
    }

    //Helper
    private ResponseEntity<Map<String, Object>> problem(
            int status, String title, String detail, String type, Map<String, Object> extra) {

        var body = new LinkedHashMap<String, Object>();
        body.put("type", type);
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        body.put("traceId", UUID.randomUUID().toString());
        extra.forEach(body::put);
        return ResponseEntity.status(status).body(body);
    }
}
