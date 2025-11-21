package com.acme.bms.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.rider.TripBillResponse;
import com.acme.bms.api.rider.TripInfoResponse;
import com.acme.bms.application.exception.TripNotFoundException;
import com.acme.bms.application.exception.TripNotActiveException;
import com.acme.bms.application.exception.UserNotFoundException;
import com.acme.bms.domain.billing.BillBuilder;
import com.acme.bms.domain.billing.NonStudentBillBuilder;
import com.acme.bms.domain.billing.StudentBillBuilder;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.repo.BillRepository;
import com.acme.bms.domain.repo.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC11_RiderCheckTripBill {

    private final TripRepository tripRepo;
    private final BillRepository billRepo;

    @Transactional
    public TripBillResponse execute(Long tripId, Long riderId) {
        if (riderId == null) {
            throw new UserNotFoundException();
        }

        Trip trip = tripRepo.findById(tripId).orElseThrow(() -> new TripNotFoundException(tripId));

        if (!trip.getRider().getId().equals(riderId)) {
            throw new UserNotFoundException();
        }

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new TripNotActiveException(tripId);
        }

        // choose builder based on if email ends with .edu
        BillBuilder builder;
        var rider = trip.getRider();
        if (rider.getEmail() != null && rider.getEmail().toLowerCase().endsWith(".edu")) {
            builder = new StudentBillBuilder();
        } else {
            builder = new NonStudentBillBuilder();
        }

        // Create bill first
        builder.createBill(trip);
        Bill bill = builder.getBill();

        // Set skipFlexDollar BEFORE calling the rest of constructBill
        if (trip.getRider() != null && trip.getId() != null &&
                trip.getId().equals(trip.getRider().getLastFlexDollarEarnedTripId())) {
            bill.setSkipFlexDollar(true);
        }

        // Now complete the bill construction with skipFlexDollar already set
        builder.calculateBaseCost();
        builder.addUsageCost();
        builder.addElectricCharge();
        builder.applyDiscount();
        builder.applyTierDiscount();
        builder.applyFlexDollar();

        bill.setCreatedAt(LocalDateTime.now());
        bill = billRepo.save(bill);

        return TripBillResponse.from(bill, new TripInfoResponse(trip));
    }
}
