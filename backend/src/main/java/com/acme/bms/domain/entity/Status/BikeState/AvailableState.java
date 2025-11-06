package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;

public class AvailableState extends BikeState {

    public AvailableState(Bike bike) {
        super(bike);
    }

    @Override
    public boolean reserveBike() {
        bike.setState(new ReservedState(bike));
        bike.setStatus(com.acme.bms.domain.entity.Status.BikeStatus.RESERVED);
        System.out.println("Bike reserved successfully.");
        return true;
    }

    @Override
    public boolean checkoutBike() {
        System.out.println("Please reserve the bike before checking out.");
        return false;
    }

    @Override
    public boolean returnBike(Dock dock) {
        System.out.println("Bike successfully returned (no action needed).");
        return false;
    }

    @Override
    public boolean sendToMaintenance() {
        bike.setState(new MaintenanceState(bike));
        bike.setStatus(com.acme.bms.domain.entity.Status.BikeStatus.MAINTENANCE);
        // mark that this bike was forced into maintenance by operator action
        bike.setMaintenanceForced(true);
        return true;
    }

    @Override
    public String toString() {
        return "Available";
    }
}
