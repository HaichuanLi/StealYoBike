package com.acme.bms.domain.entity;

public class Operator extends User {

    public Operator() {
        super();
    }

    public boolean rebalanceBikes(DockingStation fromStation, DockingStation toStation, BikeType bikeType) {
        // We will move enough bikes so that both stations have an equal number of bikes
        int amountToMove = (fromStation.getNumberOfAvailableBikes(bikeType)
                - toStation.getNumberOfAvailableBikes(bikeType)) / 2;
        for (int i = 0; i < amountToMove; i++) {
            try {

                Bike bikeToMove = fromStation.getFirstAvailableBike(bikeType);
                Dock sourceDock = bikeToMove.getDock();
                Dock targetDock = toStation.findEmptyDock();

                targetDock.setBike(bikeToMove);
                sourceDock.setBike(null);
                bikeToMove.setDock(targetDock);
            } catch (Exception e) {
                System.out.println("Not all the bikes could be moved during rebalancing.");
                return false;
            }

        }
        return true;
    }
}
