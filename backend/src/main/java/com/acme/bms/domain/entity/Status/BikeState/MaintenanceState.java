package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;

public class MaintenanceState extends BikeState {

    public MaintenanceState(Bike bike) {
        super(bike);
    }

    @Override
    public boolean reserveBike() {
        System.out.println("Bike is under maintenance and cannot be reserved.");
        return false;
    }

    @Override
    public boolean checkoutBike() {
        System.out.println("Bike is under maintenance and cannot be checked out.");
        return false;
    }

    @Override
    public boolean returnBike() {
        bike.changeState(new AvailableState(bike));
        return false;
    }
}
