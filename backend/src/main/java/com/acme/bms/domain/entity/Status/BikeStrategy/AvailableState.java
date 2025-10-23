package com.acme.bms.domain.entity.Status.BikeStrategy;

import com.acme.bms.domain.entity.Bike;

public class AvailableState extends BikeState {

    public AvailableState(Bike bike) {
        super(bike);
    }

    @Override
    public boolean reserveBike() {
        bike.setState(new ReservedState(bike));
        System.out.println("Bike reserved successfully.");
        return true;
    }

    @Override
    public boolean checkoutBike() {
        System.out.println("Please reserve the bike before checking out.");
        return true;
    }

    @Override
    public boolean returnBike() {
        System.out.println("Bike is already available.");
        return false;
    }

    @Override
    public String toString() {
        return "Available";
    }
}
