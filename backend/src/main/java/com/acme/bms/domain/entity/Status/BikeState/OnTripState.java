package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;

public class OnTripState extends BikeState {

    public OnTripState(Bike bike) {
        super(bike);
    }

    @Override
    public boolean reserveBike() {
        System.out.println("Cannot reserve a bike that is currently on a trip.");
        return false;
    }

    @Override
    public boolean checkoutBike() {
        System.out.println("Bike is already on a trip.");
        return false;
    }

    @Override
    public boolean returnBike() {
        bike.markAsAvailable();
        bike.changeState(new AvailableState(bike));
        return true;
    }
}
