package com.acme.bms.domain.entity;

import com.acme.bms.domain.entity.Status.DockStatus;

public class Operator extends User {

    public Operator() {
        super();
    }
    

    public boolean rebalanceBikes(DockingStation fromStation, DockingStation toStation) {
        //We will move enough bikes so that both stations have an equal number of bikes
        int amountToMove = (fromStation.getNumberOfAvailableBikes() - toStation.getNumberOfAvailableBikes()) / 2;
        for (int i = 0; i < amountToMove; i++) {
            try{

                Bike bikeToMove = fromStation.findAvailableBike();
                Dock sourceDock = bikeToMove.getDock();
                Dock targetDock = toStation.findEmptyDock();
                
                targetDock.setBike(bikeToMove);
                sourceDock.setBike(null);
                bikeToMove.setDock(targetDock);
            } catch (Exception e){
                System.out.println("Not all the bikes could be moved during rebalancing.");
                return false;
            }

        }
        return true;
    }
    public boolean markStationOutOfService(DockingStation station) {
        for (Dock dock : station.getDocks()) {
            dock.setStatus(DockStatus.OUT_OF_SERVICE);
        }
        return true;
    }
}
