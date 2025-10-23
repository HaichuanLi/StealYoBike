package com.acme.bms.domain.entity;

import java.time.LocalDateTime;

import com.acme.bms.domain.entity.Status.ReservationStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rider_id")
    private User rider;

    @ManyToOne
    @JoinColumn(name = "bike_id")
    private Bike bike;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private String pin;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Reservation(User rider, Bike bike) {
        this.rider = rider;
        this.bike = bike;
        this.pin = String.format("%04d", (int) (Math.random() * 10000));
        if (!bike.getState().reserveBike()) {
            throw new IllegalStateException("Bike cannot be reserved.");
        }
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusMinutes(15);
        bike.setReservationExpiry(createdAt);
    }

}
