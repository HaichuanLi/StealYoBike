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
import com.acme.bms.domain.entity.Status.BikeStatus;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.entity.Status.TripStatus;


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
            if (dock.getBike().getType() == type && dock.getBike().getStatus() == BikeStatus.AVAILABLE) {
                dock.getBike().setStatus(BikeStatus.RESERVED);
                Reservation r = new Reservation(dock.getBike(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), IDPinGenerator.generateID(), user, ReservationStatus.ACTIVE);
                reservations.add(r);
                return r;
            }
        }

        return null;
    }

    public Trip checkOutBike(User user, Reservation reservation, int pin) {
        if (reservation.getRider().getId().equals(user.getId()) || reservation.getStatus() != ReservationStatus.ACTIVE) {
            return null;
        }

        if (reservation.getPin() != pin) {
            System.out.println("Wrong PIN entered.");
            return null;
        }
        //Trying to checkout bike after reservation expired
        if (LocalDateTime.now().isAfter(reservation.getExpiresAt())) {
            reservation = reserveBike(reservation.getDockingStation(), user, reservation.getBike().getType());
            if (reservation == null) {
                System.out.println("No other bike of the same type available.");
                return null;
            }
        }
        reservation.setStatus(ReservationStatus.FULFILLED);
        Bike bike = reservation.getBike();
        Trip t = new Trip(bike, user, reservation.getBike().getDock().getStation(), LocalDateTime.now(), TripStatus.STARTED);
        bike.setStatus(BikeStatus.ON_TRIP);
        bike.getDock().setBike(null);
        bike.setDock(null);

        System.out.println("Trip has been started successfully.");
        return t;

    }

}
