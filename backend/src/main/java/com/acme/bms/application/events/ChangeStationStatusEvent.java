package com.acme.bms.application.events;

import com.acme.bms.domain.entity.Status.DockingStationStatus;

public record ChangeStationStatusEvent(Long stationId, DockingStationStatus status){}

