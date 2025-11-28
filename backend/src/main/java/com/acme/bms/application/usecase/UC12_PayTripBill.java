package com.acme.bms.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.rider.TripBillResponse;
import com.acme.bms.application.exception.TripNotFoundException;
import com.acme.bms.application.exception.UserNotFoundException;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.repo.BillRepository;
import com.acme.bms.domain.repo.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC12_PayTripBill {

    private final TripRepository tripRepo;
    private final BillRepository billRepo;
    private final UC11_RiderCheckTripBill billCreator;

    @Transactional
    public TripBillResponse execute(Long tripId, String paymentToken, Long riderId) {
        if (riderId == null) {
            throw new UserNotFoundException();
        }

        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        if (!trip.getRider().getId().equals(riderId)) {
            throw new UserNotFoundException();
        }

        Bill bill = billRepo.findByTripId(tripId).orElseGet(() -> {
            // UC11 will create + save the bill (with flex + discounts)
            billCreator.execute(tripId, riderId);
            return billRepo.findByTripId(tripId)
                    .orElseThrow(() -> new TripNotFoundException(tripId));
        });

        bill.setPaid(true);
        bill.setPaymentTokenUsed(paymentToken);
        bill.setPaidAt(LocalDateTime.now());

        bill = billRepo.save(bill);

        return TripBillResponse.from(bill,
                new com.acme.bms.api.rider.TripInfoResponse(trip));
    }
}
