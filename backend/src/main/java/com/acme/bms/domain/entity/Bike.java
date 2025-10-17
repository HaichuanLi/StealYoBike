package com.acme.bms.domain.entity;

import java.time.LocalDateTime;

import com.acme.bms.domain.entity.Status.BikeState.AvailableState;
import com.acme.bms.domain.entity.Status.BikeState.BikeState;
import com.acme.bms.domain.entity.Status.BikeState.MaintenanceState;
import com.acme.bms.domain.entity.Status.BikeState.OnTripState;
import com.acme.bms.domain.entity.Status.BikeState.ReservedState;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bikes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Enumerated(EnumType.STRING)
    private BikeType type;

    BikeState state;

    @ManyToOne
    @JoinColumn(name = "dock_id")
    private Dock dock; // null when undocked or on trip

    private java.time.LocalDateTime reservationExpiry;

    public void changeState(BikeState state) {
        this.state = state;
        System.out.println("Bike " + id + " changed to " + state.getClass().getSimpleName());
    }

    public boolean reserveBike() {
        return state.reserveBike();
    }

    public boolean checkoutBike() {
        return state.checkoutBike();
    }

    public boolean returnBike() {
        return state.returnBike();
    }

    public void markAsAvailable() {
        System.out.println("Bike " + id + " is now available for use.");
    }

    public void markAsInUse() {
        System.out.println("Bike " + id + " is now on a trip.");
    }

    public void markAsReserved() {
        System.out.println("Bike " + id + " is reserved for a user.");
    }

    public void markAsUnderMaintenance() {
        System.out.println("Bike " + id + " is under maintenance.");
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BikeType getType() {
        return type;
    }

    public void setType(BikeType type) {
        this.type = type;
    }

    public String getState() {
        if (state instanceof AvailableState){
            return "AVAILABLE";
        }
        else if (state instanceof ReservedState){
            return "RESERVED";
        }
        else if (state instanceof OnTripState){
            return "ON_TRIP";
        }
        else if (state instanceof MaintenanceState){
            return "MAINTENANCE";
        }
        return null;
    }

    public Dock getDock() {
        return dock;
    }

    public void setDock(Dock dock) {
        this.dock = dock;
    }

    public LocalDateTime getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(LocalDateTime reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }
}
