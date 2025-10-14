package com.acme.bms.application.events;

public record UserLoggedInEvent (Long userId, String role, String email ){}

