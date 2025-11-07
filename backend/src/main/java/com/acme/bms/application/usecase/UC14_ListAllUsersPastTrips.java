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

import java.util.List;
import java.util.stream.Collectors;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UC14_ListAllUsersPastTrips {

    private final TripRepository tripRepository;
    private final BillRepository billRepository;

    @Transactional(readOnly = true)
    public List<PastTripResponse> execute() {
        // Get all completed trips, ordered by end time desc
        List<Trip> trips = tripRepository.findAllByStatusOrderByEndTimeDesc(TripStatus.COMPLETED);
        
        // Convert to PastTripResponse objects
        return trips.stream().map(trip -> {
            Optional<Bill> bill = billRepository.findByTripId(trip.getId());
            
            String startStationName = trip.getStartStation() != null 
                ? trip.getStartStation().getName()
                : null;
            String endStationName = trip.getEndStation() != null 
                ? trip.getEndStation().getName()
                : null;
            
            long durationMinutes = trip.getStartTime() != null && trip.getEndTime() != null
                ? Duration.between(trip.getStartTime(), trip.getEndTime()).toMinutes()
                : 0;

            double total = bill.map(b -> b.getTotalAmount()).orElse((double) trip.getPriceCents() / 100.0);
            boolean paid = bill.map(b -> b.isPaid()).orElse(false);
            Long billId = bill.map(b -> b.getId()).orElse(null);

            return new PastTripResponse(
                trip.getId(),
                trip.getBike() != null ? trip.getBike().getId() : null,
                trip.getBike() != null ? trip.getBike().getType().name() : null,
                trip.getStartTime() != null ? trip.getStartTime().toString() : null,
                trip.getEndTime() != null ? trip.getEndTime().toString() : null,
                (int) durationMinutes,
                startStationName,
                endStationName,
                total,
                paid,
                billId,
                trip.getRider() != null ? trip.getRider().getId() : null,
                trip.getRider() != null ? trip.getRider().getUsername() : null
            );
        }).collect(Collectors.toList());
    }
}