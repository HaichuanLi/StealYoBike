package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;

public class ReservedState extends BikeState {

    public ReservedState(Bike bike) {
        super(bike);
    }

    @Override
    public boolean reserveBike() {
        System.out.println("Bike is already reserved.");
        return false;
    }

    @Override
    public boolean checkoutBike() {
        bike.setState(new OnTripState(bike));
        System.out.println("Bike checked out successfully.");
        return true;
    }

    @Override
    public boolean returnBike(Dock dock) {
        if (dock.getBike().equals(bike)) {
            bike.setState(new AvailableState(bike));
            if (bike.getReservationExpiry().isAfter(java.time.Instant.now())) {
                System.out.println("Bike reservation expired. Bike is now available.");
            } else {
                System.out.println("Bike reservation cancelled and is now available.");
            }
            bike.setReservationExpiry(null);
            return true;
        }
        System.out.println("Bike is reserved and cannot be returned.");
        return false;
    }

    @Override
    public String toString() {
        return "Reserved";
    }

}
