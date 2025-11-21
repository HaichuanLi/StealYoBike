package com.acme.bms.application.events;

import com.acme.bms.domain.entity.Status.StationStatus;

public record ChangeStationStatusEvent(Long stationId, StationStatus status) {
}
