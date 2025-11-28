package com.acme.bms.api.error;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.hibernate.LazyInitializationException;

import com.acme.bms.application.exception.*;

import static org.mockito.Mockito.*;

public class ApiErrorsTest {

    private final ApiErrors apiErrors = new ApiErrors();

    @Test
    public void validation_returnsProperErrorFormat() {
        // Mock MethodArgumentNotValidException
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "must be valid email");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = apiErrors.validation(ex);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().get("title"));
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    public void invalidCredentials_returns401() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        ResponseEntity<Map<String, Object>> response = apiErrors.unauthorized(ex);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Unauthorized", response.getBody().get("title"));
        assertEquals("Invalid username, email, or password", response.getBody().get("detail"));
    }

    @Test
    public void userNotFound_returns401() {
        UserNotFoundException ex = new UserNotFoundException();

        ResponseEntity<Map<String, Object>> response = apiErrors.userNotFound(ex);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Unauthorized", response.getBody().get("title"));
        assertEquals("Authenticated user not found", response.getBody().get("detail"));
    }

    @Test
    public void operatorNotFound_returns404() {
        OperatorNotFoundException ex = new OperatorNotFoundException();

        ResponseEntity<Map<String, Object>> response = apiErrors.operatorNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().get("title"));
        assertEquals("Operator not found", response.getBody().get("detail"));
    }

    @Test
    public void forbiddenOperation_returns403() {
        ForbiddenOperationException ex = new ForbiddenOperationException("Access denied");

        ResponseEntity<Map<String, Object>> response = apiErrors.forbidden(ex);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Forbidden", response.getBody().get("title"));
    }

    @Test
    public void emailAlreadyUsed_returns409() {
        EmailAlreadyUsedException ex = new EmailAlreadyUsedException();

        ResponseEntity<Map<String, Object>> response = apiErrors.conflict(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict", response.getBody().get("title"));
        assertEquals("Email already in use", response.getBody().get("detail"));
    }

    @Test
    public void usernameAlreadyUsed_returns409() {
        UsernameAlreadyUsedException ex = new UsernameAlreadyUsedException();

        ResponseEntity<Map<String, Object>> response = apiErrors.conflict(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict", response.getBody().get("title"));
        assertEquals("Username already in use", response.getBody().get("detail"));
    }

    @Test
    public void stationNotFound_returns404() {
        StationNotFoundException ex = new StationNotFoundException(1L);

        ResponseEntity<Map<String, Object>> response = apiErrors.stationNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().get("title"));
        assertEquals("Station not found: 1", response.getBody().get("detail"));
    }

    @Test
    public void tripNotFound_returns404() {
        TripNotFoundException ex = new TripNotFoundException(1L);

        ResponseEntity<Map<String, Object>> response = apiErrors.tripNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().get("title"));
        assertEquals("Trip not found: 1", response.getBody().get("detail"));
    }

    @Test
    public void reservationNotFound_returns404() {
        ReservationNotFoundException ex = new ReservationNotFoundException();

        ResponseEntity<Map<String, Object>> response = apiErrors.reservationNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().get("title"));
        assertEquals("No active reservation found for the user", response.getBody().get("detail"));
    }

    @Test
    public void noAvailableBikes_returns422() {
        NoAvailableBikesException ex = new NoAvailableBikesException(1L, "REGULAR");

        ResponseEntity<Map<String, Object>> response = apiErrors.noBikes(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals("No bikes available", response.getBody().get("title"));
        assertEquals("No available bikes of type REGULAR at station 1", response.getBody().get("detail"));
    }

    @Test
    public void activeReservationExists_returns409() {
        ActiveReservationOrTripExistsException ex = new ActiveReservationOrTripExistsException();

        ResponseEntity<Map<String, Object>> response = apiErrors.activeReservationOrTripExists(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict", response.getBody().get("title"));
    }

    @Test
    public void tripNotActive_returns422() {
        TripNotActiveException ex = new TripNotActiveException(1L);

        ResponseEntity<Map<String, Object>> response = apiErrors.tripNotActive(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals("Unprocessable Entity", response.getBody().get("title"));
        assertEquals("Trip 1 is not active or already completed", response.getBody().get("detail"));
    }

    @Test
    public void noEmptyDock_returns422() {
        NoEmptyDockAvailableException ex = new NoEmptyDockAvailableException(1L);

        ResponseEntity<Map<String, Object>> response = apiErrors.noEmptyDock(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals("No empty dock", response.getBody().get("title"));
        assertEquals("No empty dock available at station 1", response.getBody().get("detail"));
    }

    @Test
    public void bikeReturnFailed_returns409() {
        BikeReturnFailedException ex = new BikeReturnFailedException(1L, 2L);

        ResponseEntity<Map<String, Object>> response = apiErrors.bikeReturnFailed(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Return failed", response.getBody().get("title"));
        assertEquals("Bike 1 could not be returned to dock 2", response.getBody().get("detail"));
    }

    @Test
    public void bikeNotFound_returns404() {
        BikeNotFoundException ex = new BikeNotFoundException(1L);

        ResponseEntity<Map<String, Object>> response = apiErrors.bikeNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().get("title"));
        assertEquals("Bike not found: 1", response.getBody().get("detail"));
    }

    @Test
    public void bikeMaintenanceState_returns422() {
        BikeMaintenanceStateException ex = new BikeMaintenanceStateException();

        ResponseEntity<Map<String, Object>> response = apiErrors.bikeStateInvalid(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals("Unprocessable Entity", response.getBody().get("title"));
        assertEquals("Bike cannot be sent to maintenance in its current state", response.getBody().get("detail"));
    }

    @Test
    public void lazyInitialization_returns500() {
        LazyInitializationException ex = new LazyInitializationException("Lazy load error");

        ResponseEntity<Map<String, Object>> response = apiErrors.lazyInitialization(ex);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Data access error", response.getBody().get("title"));
    }

    @Test
    public void ioException_brokenPipe_returns200() {
        java.io.IOException ex = new java.io.IOException("Broken pipe");

        ResponseEntity<Map<String, Object>> response = apiErrors.ioException(ex);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void ioException_other_returns500() {
        java.io.IOException ex = new java.io.IOException("File not found");

        ResponseEntity<Map<String, Object>> response = apiErrors.ioException(ex);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    public void asyncTimeout_returns204() {
        org.springframework.web.context.request.async.AsyncRequestTimeoutException ex = new org.springframework.web.context.request.async.AsyncRequestTimeoutException();

        ResponseEntity<Map<String, Object>> response = apiErrors.asyncTimeout(ex);

        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    public void fallbackException_returns500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<Map<String, Object>> response = apiErrors.fallback(ex);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal Server Error", response.getBody().get("title"));
        assertNotNull(response.getBody().get("traceId"));
    }

    @Test
    public void stationAlreadyOutOfService_returns409() {
        StationAlreadyOutOfServiceException ex = new StationAlreadyOutOfServiceException(1L);

        ResponseEntity<Map<String, Object>> response = apiErrors.alreadyOos(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict", response.getBody().get("title"));
        assertEquals("Station 1 is already out of service", response.getBody().get("detail"));
    }
}
