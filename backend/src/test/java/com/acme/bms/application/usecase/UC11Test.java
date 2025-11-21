package com.acme.bms.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.acme.bms.api.rider.TripBillResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.repo.BillRepository;
import com.acme.bms.domain.repo.TripRepository;

class UC11Test {

    @Test
    void studentGetsStudentBillCalculation() {
        TripRepository tripRepo = mock(TripRepository.class);
        BillRepository billRepo = mock(BillRepository.class);

        UC11_RiderCheckTripBill sut = new UC11_RiderCheckTripBill(tripRepo, billRepo);

        User rider = User.builder().id(101L).email("123123@school.edu").username("123").fullName("123")
                .passwordHash("x").build();
        Bike bike = Bike.builder().type(BikeType.REGULAR).build();

        DockingStation start = DockingStation.builder().id(1L).name("Start Station").build();

        Trip trip = Trip.builder()
                .id(11L)
                .rider(rider)
                .bike(bike)
                .startStation(start)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now())
                .status(TripStatus.COMPLETED)
                .build();

        when(tripRepo.findById(11L)).thenReturn(Optional.of(trip));
        when(billRepo.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        TripBillResponse resp = sut.execute(11L, 101L);

        // expected for student regular 10 minutes: base 2 + usage 10*0.10 = 3.0 -> 10%
        // discount = 2.7
        assertEquals(2.7, resp.totalAmount(), 0.0001);
    }

    @Test
    void nonStudentElectricGetsNonStudentCalculation() {
        TripRepository tripRepo = mock(TripRepository.class);
        BillRepository billRepo = mock(BillRepository.class);

        UC11_RiderCheckTripBill sut = new UC11_RiderCheckTripBill(tripRepo, billRepo);

        User rider = User.builder().id(102L).email("bob@example.com").username("bob").fullName("Bob").passwordHash("x")
                .build();
        Bike bike = Bike.builder().type(BikeType.ELECTRIC).build();

        DockingStation start2 = DockingStation.builder().id(2L).name("Start Station 2").build();

        Trip trip = Trip.builder()
                .id(12L)
                .rider(rider)
                .bike(bike)
                .startStation(start2)
                .startTime(LocalDateTime.now().minusMinutes(20))
                .endTime(LocalDateTime.now())
                .status(TripStatus.COMPLETED)
                .build();

        when(tripRepo.findById(12L)).thenReturn(Optional.of(trip));
        when(billRepo.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        TripBillResponse resp = sut.execute(12L, 102L);

        // expected for non-student electric 20 minutes: base 2.5 + usage 20*0.15 = 5.5
        // -> *1.5 = 8.25
        assertEquals(8.25, resp.totalAmount(), 0.0001);
    }
}
