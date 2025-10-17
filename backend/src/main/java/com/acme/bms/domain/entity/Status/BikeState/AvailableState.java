package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;

public class AvailableState extends BikeState {
    public AvailableState(Bike bike) {
        super(bike);
    }

    @Override
    public boolean reserveBike() {
        bike.markAsReserved();
        bike.changeState(new ReservedState(bike));
        return true;
    }

    @Override
    public boolean checkoutBike() {
        System.out.println("Bike is available. Please reserve it before checkout.");
        return false;
    }

    @Override
    public boolean returnBike() {
        System.out.println("Cannot return a bike that is in available state.");
        return false;
    }
}
