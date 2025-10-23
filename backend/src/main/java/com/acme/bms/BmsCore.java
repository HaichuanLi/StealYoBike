package com.acme.bms;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Reservation;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.entity.Status.BikeState.BikeState;

public class BmsCore {

    private List<DockingStation> dockStations;
    private List<Reservation> reservations;

    public BmsCore() {
        dockStations = new ArrayList<>();
        reservations = new ArrayList<>();
    }

    public Reservation reserveBike(DockingStation dockStation, User user, BikeType type) {
        //User already has an active or fulfilled reservation
        for (Reservation r : reservations) {
            if (r.getRider().getId().equals(user.getId()) && (r.getStatus() == ReservationStatus.ACTIVE || r.getStatus() == ReservationStatus.FULFILLED)) {
                return null;
            }
        }
        for (Dock dock : dockStation.getDocks()) {
            if (dock.getBike().getType() == type) {
                if (dock.getBike().reserveBike()){
                    Reservation r = new Reservation(dock.getBike(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), IDPinGenerator.generateID(), user, ReservationStatus.ACTIVE);
                    reservations.add(r);
                    return r;
                }
                
            }
        }
        System.out.println("No available bike of the requested type.");
        return null;
    }

    public Trip checkoutBike(User user, Reservation reservation, int pin) {
        if (reservation.getRider().getId().equals(user.getId()) || reservation.getStatus() != ReservationStatus.ACTIVE) {
            return null;
        }

        if (reservation.getPin() != pin) {
            System.out.println("Wrong PIN entered.");
            return null;
        }
        //Trying to checkout bike after reservation expired
        if (LocalDateTime.now().isAfter(reservation.getExpiresAt())) {
            System.out.println("Reservation has expired. Now trying to find another available bike of the same type.");

            reservation = reserveBike(reservation.getDockingStation(), user, reservation.getBike().getType());
            if (reservation == null) {
                System.out.println("No other bike of the same type available at this docking station.");
                return null;
            }
        }
        reservation.setStatus(ReservationStatus.FULFILLED);
        Bike bike = reservation.getBike();
        Trip t = new Trip(bike, user, reservation.getBike().getDock().getStation(), LocalDateTime.now(), TripStatus.STARTED);
        if (!bike.checkoutBike()){
            System.out.println("Failed to checkout the bike.");
            return null;
        }

        System.out.println("Trip has been started successfully.");
        return t;

    }

    public boolean returnBike(Trip trip, DockingStation station) {
        if (trip.getStatus() != TripStatus.STARTED) {
            System.out.println("Trip is not active.");
            return false;
        }

        for (Dock dock : station.getDocks()) {
            if (dock.getStatus() == DockStatus.EMPTY) {
                if (trip.getBike().returnBike()){
                    trip.setEndTime(LocalDateTime.now());
                    trip.setEndStation(station);
                    trip.setStatus(TripStatus.COMPLETED);
                    dock.setBike(trip.getBike());
                    trip.getBike().setDock(dock);
                    System.out.println("Bike returned successfully.");
                    return true;
                }
            }
        }
        System.out.println("Bike was successfully returned.");
        return true;
    }
    public int countAllAvailableBikes(DockingStation station) {
        int count = 0;
        for (Dock dock : station.getDocks()) {
            if (dock.getBike() != null && dock.getBike().getState().equals("AVAILABLE")) {
                count++;
            }
        }
        return count;
    }
    public int countAvailableRegularBikes(DockingStation station) {
        int count = 0;
        for (Dock dock : station.getDocks()) {
            if (dock.getBike() != null && dock.getBike().getState().equals("AVAILABLE") && dock.getBike().getType() == BikeType.REGULAR) {
                count++;
            }
        }
        return count;
    }
    public int countAvailableElectricBikes(DockingStation station) {
        int count = 0;
        for (Dock dock : station.getDocks()) {
            if (dock.getBike() != null && dock.getBike().getState().equals("AVAILABLE") && dock.getBike().getType() == BikeType.ELECTRIC) {
                count++;
            }
        }
        return count;
    }

}
