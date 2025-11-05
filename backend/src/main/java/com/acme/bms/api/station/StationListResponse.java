package com.acme.bms.api.station;

import java.util.List;

public record StationListResponse(
                List<StationSummary> stations) {
        public record StationSummary(
                        Long stationId,
                        String name,
                        String status,
                        Double latitude,
                        Double longitude,
                        String streetAddress,
                        int availableBikes,
                        int availableDocks,
                        int capacity) {
        }

        public static StationListResponse fromEntities(
                        List<com.acme.bms.domain.entity.DockingStation> stationEntities) {
                return new StationListResponse(stationEntities.stream().map(station -> new StationSummary(
                                station.getId(),
                                station.getName(),
                                station.getStatus().name(),
                                station.getLatitude(),
                                station.getLongitude(),
                                station.getStreetAddress(),
                                station.getDocks().stream().filter(dock -> dock.getBike() != null).toList().size(),
                                (int) station.getDocks().stream().filter(dock -> dock.getBike() == null).count(),
                                station.getCapacity())).toList());
        }

        public static StationListResponse fromProjections(
                        List<StationSummaryDto> projections) {
                return new StationListResponse(projections.stream().map(p -> new StationSummary(
                                p.stationId,
                                p.name,
                                p.status == null ? null : p.status.name(),
                                p.latitude,
                                p.longitude,
                                p.streetAddress,
                                p.availableBikes.intValue(),
                                p.availableDocks.intValue(),
                                p.capacity)).toList());
        }
}
