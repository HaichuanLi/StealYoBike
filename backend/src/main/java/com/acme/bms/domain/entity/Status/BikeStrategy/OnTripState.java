package com.acme.bms.domain.entity.Status.BikeStrategy;

import com.acme.bms.domain.entity.Bike;

public class OnTripState extends BikeState {

    public OnTripState(Bike bike) {
        super(bike);
    }

    @Override
    public boolean reserveBike() {
        System.out.println("Bike is currently on a trip and cannot be reserved.");
        return false;
    }

    @Override
    public boolean checkoutBike() {
        System.out.println("Bike is already checked out.");
        return false;
    }

    @Override
    public boolean returnBike() {
        bike.setState(new AvailableState(bike));
        System.out.println("Bike returned successfully and is now available.");
        return true;
    }

    @Override
    public String toString() {
        return "On Trip";
    }

}
