package com.acme.bms.application.events;

public record OperatorSendBikeToMaintenanceEvent(Long operatorId, Long bikeId) {}
