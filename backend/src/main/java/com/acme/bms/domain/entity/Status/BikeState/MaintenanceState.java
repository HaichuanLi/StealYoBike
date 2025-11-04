package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;

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
    public boolean returnBike(Dock dock) {
        bike.setState(new AvailableState(bike));
        bike.setDock(dock);
        System.out.println("Bike maintenance completed and is now available.");
        return true;
    }

    @Override
    public boolean sendToMaintenance() {
    System.out.println("Bike is already in maintenance.");
    return false; 
    }   


    @Override
    public String toString() {
        return "Maintenance";
    }

}
