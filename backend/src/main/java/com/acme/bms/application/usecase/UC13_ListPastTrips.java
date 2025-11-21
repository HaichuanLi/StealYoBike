package com.acme.bms.application.usecase;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.acme.bms.api.rider.PastTripResponse;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.repo.BillRepository;
import com.acme.bms.domain.repo.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC13_ListPastTrips {

    private final TripRepository tripRepo;
    private final BillRepository billRepo;

    public List<PastTripResponse> execute(
            Long riderId,
            LocalDate fromDate, // inclusive (00:00)
            LocalDate toDate, // inclusive (23:59:59.999999999)
            String bikeType // STANDARD | E_BIKE (case-insensitive)
    ) {
        List<Trip> trips = tripRepo.findAllByRiderIdAndStatus(riderId, TripStatus.COMPLETED);

        final LocalDateTime startBound = (fromDate != null) ? fromDate.atStartOfDay() : null;
        final LocalDateTime endBound = (toDate != null) ? toDate.plusDays(1).atStartOfDay().minusNanos(1) : null;
        final String typeNorm = (bikeType != null && !bikeType.isBlank()) ? bikeType.toUpperCase() : null;

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return trips.stream()
                // date range (by trip start time; inclusive)
                .filter(t -> {
                    if (t.getStartTime() == null)
                        return false;
                    if (startBound != null && t.getStartTime().isBefore(startBound))
                        return false;
                    if (endBound != null && t.getStartTime().isAfter(endBound))
                        return false;
                    return true;
                })
                // bike type
                .filter(t -> {
                    if (typeNorm == null)
                        return true;
                    return t.getBike() != null
                            && t.getBike().getType() != null
                            && typeNorm.equalsIgnoreCase(t.getBike().getType().name());
                })
                .map(t -> {
                    Long tripId = t.getId();
                    Long bikeId = t.getBike() != null ? t.getBike().getId() : null;
                    String bikeTypeStr = t.getBike() != null && t.getBike().getType() != null
                            ? t.getBike().getType().name()
                            : null;
                    String startTime = t.getStartTime() != null ? t.getStartTime().format(fmt) : null;
                    String endTime = t.getEndTime() != null ? t.getEndTime().format(fmt) : null;
                    Integer duration = (t.getStartTime() != null && t.getEndTime() != null)
                            ? (int) Duration.between(t.getStartTime(), t.getEndTime()).toMinutes()
                            : null;
                    String startStation = t.getStartStation() != null ? t.getStartStation().getName() : null;
                    String endStation = t.getEndStation() != null ? t.getEndStation().getName() : null;

                    Bill bill = billRepo.findByTripId(tripId).orElse(null);
                    Double total = bill != null ? bill.getTotalAmount() : 0.0;
                    Boolean paid = bill != null ? bill.isPaid() : false;
                    Long billId = bill != null ? bill.getId() : null;

                    return new PastTripResponse(tripId, bikeId, bikeTypeStr, startTime, endTime, duration,
                            startStation, endStation, total, paid, billId, riderId,
                            t.getRider() != null ? t.getRider().getUsername() : null);
                })
                .collect(Collectors.toList());
    }

    public List<PastTripResponse> execute(Long riderId) {
        return execute(riderId, null, null, null);
    }
}
