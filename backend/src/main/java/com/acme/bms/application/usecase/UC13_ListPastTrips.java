package com.acme.bms.application.usecase;

import java.time.Duration;
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

    public List<PastTripResponse> execute(Long riderId) {
        List<Trip> trips = tripRepo.findAllByRiderIdAndStatus(riderId, TripStatus.COMPLETED);

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return trips.stream().map(t -> {
            Long tripId = t.getId();
            Long bikeId = t.getBike() != null ? t.getBike().getId() : null;
            String bikeType = t.getBike() != null && t.getBike().getType() != null ? t.getBike().getType().name() : null;
            String startTime = t.getStartTime() != null ? t.getStartTime().format(fmt) : null;
            String endTime = t.getEndTime() != null ? t.getEndTime().format(fmt) : null;
            Integer duration = null;
            if (t.getStartTime() != null && t.getEndTime() != null) {
                duration = (int) Duration.between(t.getStartTime(), t.getEndTime()).toMinutes();
            }
            String startStation = t.getStartStation() != null ? t.getStartStation().getName() : null;
            String endStation = t.getEndStation() != null ? t.getEndStation().getName() : null;

            Bill bill = billRepo.findByTripId(tripId).orElse(null);
            Double total = bill != null ? bill.getTotalAmount() : 0.0;
            Boolean paid = bill != null ? bill.isPaid() : false;
            Long billId = bill != null ? bill.getId() : null;

            return new PastTripResponse(tripId, bikeId, bikeType, startTime, endTime, duration, startStation, endStation, 
                total, paid, billId, riderId, t.getRider() != null ? t.getRider().getUsername() : null);
        }).collect(Collectors.toList());
    }
}
