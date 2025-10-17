package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;

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
        bike.markAsInUse();
        bike.changeState(new OnTripState(bike));
        bike.getDock().setBike(null);
        bike.setDock(null);
        return true;
    }

    @Override
    public boolean returnBike() {
        System.out.println("Cannot return a bike that is reserved but not checked out.");
        return false;
    }
}
