package com.acme.bms.application.events;

public record UserRegisteredEvent(Long userId, String role, String email) {}
