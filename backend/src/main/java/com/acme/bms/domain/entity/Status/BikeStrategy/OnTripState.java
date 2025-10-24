package com.acme.bms.domain.entity.Status.BikeStrategy;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.Status.DockStatus;

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
    public boolean returnBike(Dock dock) {
        if (dock == null) {
            System.out.println("No dock provided.");
            return false;
        }
        if (dock.getStatus() != DockStatus.EMPTY) {
            System.out.println("Dock is not empty.");
            return false;
        }
        // Type gate
        if (!dock.accepts(bike.getType())) {
            System.out.println("Dock does not accept bike type " + bike.getType());
            return false;
        }

        // extra guard: don't accidentally overwrite
        if (dock.getBike() != null) {
            System.out.println("Dock already has a bike.");
            return false;
        }
        // Perform the state transition and maintain both sides of the link
        dock.setBike(bike);
        dock.setStatus(DockStatus.OCCUPIED);
        bike.setDock(dock);
        bike.setState(new AvailableState(bike));
        System.out.println("Bike returned successfully and is now available.");
        return true;
    }


    @Override
    public String toString() {
        return "On Trip";
    }

}
