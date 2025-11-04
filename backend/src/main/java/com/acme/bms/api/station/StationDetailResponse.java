package com.acme.bms.api.station;

import java.util.List;

public record StationDetailResponse(
                Long stationId,
                String name,
                String status,
                Double latitude,
                Double longitude,
                String streetAddress,
                int capacity,
                int expiresAfterMinutes,
                List<DockView> docks) {
        public record BikeView(
                        Long bikeId,
                        String type,
                        String status,
                        String reservationExpiry) {
        }

        public record DockView(
                        Long dockId,
                        String status,
                        BikeView bike) {
        }

        public StationDetailResponse(com.acme.bms.domain.entity.DockingStation station) {
                this(
                                station.getId(),
                                station.getName(),
                                station.getStatus().name(),
                                station.getLatitude(),
                                station.getLongitude(),
                                station.getStreetAddress(),
                                station.getCapacity(),
                                station.getExpiresAfterMinutes(),
                                station.getDocks().stream().map(dock -> new DockView(
                                                dock.getId(),
                                                dock.getStatus().name(),
                                                dock.getBike() != null ? new BikeView(
                                                                dock.getBike().getId(),
                                                                dock.getBike().getType().name(),
                                                                dock.getBike().getState().toString(),
                                                                dock.getBike().getReservationExpiry() != null
                                                                                ? dock.getBike().getReservationExpiry()
                                                                                                .toString()
                                                                                : null)
                                                                : null))
                                                .toList());
        }
}
