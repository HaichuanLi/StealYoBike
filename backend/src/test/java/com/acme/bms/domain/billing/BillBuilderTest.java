package com.acme.bms.domain.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;

public class BillBuilderTest {

    @Test
    void studentBillCalculation_nonElectric() {
        Bike bike = Bike.builder().type(BikeType.REGULAR).build();
        Trip trip = Trip.builder().bike(bike)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now())
                .build();

        BillBuilderDirector director = new BillBuilderDirector();

        StudentBillBuilder strudentbuilder = new StudentBillBuilder();
        director.constructBill(strudentbuilder, trip);
        Bill studentBill = strudentbuilder.getBill();

        // expected: base 2 + usage (10 * 0.10) = 3.0, after 10% discount -> 2.7
        assertEquals(2.7, studentBill.getTotalAmount(), 0.0001);
    }

    @Test
    void nonStudentBillCalculation_electric() {
        Bike bike = Bike.builder().type(BikeType.ELECTRIC).build();
        Trip trip = Trip.builder().bike(bike)
                .startTime(LocalDateTime.now().minusMinutes(20))
                .endTime(LocalDateTime.now())
                .build();
        BillBuilderDirector director = new BillBuilderDirector();

        NonStudentBillBuilder builder = new NonStudentBillBuilder();
        director.constructBill(builder, trip);
        Bill bill = builder.getBill();

        // expected using NonStudentBillBuilder: base 2.50 + usage (20 * 0.15) = 5.50,
        // electric multiplier 1.5 => 8.25
        assertEquals(8.25, bill.getTotalAmount(), 0.0001);
    }

}
