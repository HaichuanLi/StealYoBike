package com.acme.bms.domain.entity;

import com.acme.bms.domain.entity.Status.ReservationStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

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
    private Instant createdAt;
    private Instant expiresAt;

    // Required by JPA
    protected Reservation() {
    }

    // Custom constructor for app logic
    public Reservation(User rider, Bike bike) {
        this.rider = rider;
        this.bike = bike;
        this.pin = String.format("%04d", (int) (Math.random() * 10000));

        if (!bike.reserveBike()) {
            throw new IllegalStateException("Bike cannot be reserved.");
        }
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plusSeconds(300); // 5 minutes
        bike.setReservationExpiry(this.expiresAt);
    }

    public void cancelReservation() {
        if (this.status == ReservationStatus.ACTIVE) {
            this.status = ReservationStatus.CANCELLED;
            // Bike is still in its dock when reservation is cancelled
            // Use returnBike() with the bike's current dock to transition back to AVAILABLE
            if (this.bike.getDock() != null) {
                this.bike.getState().returnBike(this.bike.getDock());
            }
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
