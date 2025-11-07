package com.acme.bms.application.usecase;

import com.acme.bms.api.rider.PastTripResponse;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.repo.TripRepository;
import com.acme.bms.domain.repo.BillRepository;
import com.acme.bms.domain.entity.Status.TripStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UC14_ListAllUsersPastTrips {

    private final TripRepository tripRepository;
    private final BillRepository billRepository;

    @Transactional(readOnly = true)
    public List<PastTripResponse> execute(LocalDate fromDate, LocalDate toDate, String bikeType) {
        List<Trip> trips = tripRepository.findAllByStatusOrderByEndTimeDesc(TripStatus.COMPLETED);

        final LocalDateTime startBound = (fromDate != null) ? fromDate.atStartOfDay() : null;
        final LocalDateTime endBound = (toDate != null) ? toDate.plusDays(1).atStartOfDay().minusNanos(1) : null;
        final String typeNorm = (bikeType != null && !bikeType.isBlank()) ? bikeType.toUpperCase() : null;

        return trips.stream()
                .filter(t -> {
                    if (t.getStartTime() == null) return false;
                    if (startBound != null && t.getStartTime().isBefore(startBound)) return false;
                    if (endBound != null && t.getStartTime().isAfter(endBound)) return false;
                    return true;
                })
                .filter(t -> {
                    if (typeNorm == null) return true;
                    return t.getBike() != null
                            && t.getBike().getType() != null
                            && typeNorm.equalsIgnoreCase(t.getBike().getType().name());
                })
                .map(trip -> {
                    Optional<Bill> bill = billRepository.findByTripId(trip.getId());

                    long durationMinutes = (trip.getStartTime() != null && trip.getEndTime() != null)
                            ? Duration.between(trip.getStartTime(), trip.getEndTime()).toMinutes()
                            : 0;

                    double total = bill.map(Bill::getTotalAmount).orElse((double) trip.getPriceCents() / 100.0);
                    boolean paid = bill.map(Bill::isPaid).orElse(false);
                    Long billId = bill.map(Bill::getId).orElse(null);

                    return new PastTripResponse(
                            trip.getId(),
                            trip.getBike() != null ? trip.getBike().getId() : null,
                            trip.getBike() != null ? trip.getBike().getType().name() : null,
                            trip.getStartTime() != null ? trip.getStartTime().toString() : null,
                            trip.getEndTime() != null ? trip.getEndTime().toString() : null,
                            (int) durationMinutes,
                            trip.getStartStation() != null ? trip.getStartStation().getName() : null,
                            trip.getEndStation() != null ? trip.getEndStation().getName() : null,
                            total,
                            paid,
                            billId,
                            trip.getRider() != null ? trip.getRider().getId() : null,
                            trip.getRider() != null ? trip.getRider().getUsername() : null
                    );
                })
                .collect(Collectors.toList());
    }

    /** Backward-compatible (no filters) */
    public List<PastTripResponse> execute() {
        return execute(null, null, null);
    }
}
