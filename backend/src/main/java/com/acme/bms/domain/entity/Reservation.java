package com.acme.bms.domain.entity;

import java.time.LocalDateTime;

import com.acme.bms.IDPinGenerator;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @ManyToOne
    @JoinColumn(name = "rider_id")
    private User rider;

    @ManyToOne
    @JoinColumn(name = "bike_id")
    private Bike bike;
    private DockingStation dockingStation;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private int pin;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Reservation(Bike bike, LocalDateTime createdAt, LocalDateTime expiresAt, String id, User rider, ReservationStatus status) {
        this.bike = bike;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.id = id;
        this.pin = IDPinGenerator.generatePin();
        this.rider = rider;
        this.status = status;
        this.dockingStation = bike.getDock().getStation();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getRider() {
        return rider;
    }

    public void setRider(User rider) {
        this.rider = rider;
    }

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public DockingStation getDockingStation() {
        return dockingStation;
    }
}
