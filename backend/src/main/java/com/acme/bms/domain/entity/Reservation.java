package com.acme.bms.domain.entity;

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

    private Thread timer;

    public Reservation(User rider, Bike bike) {
        this.rider = rider;
        this.bike = bike;
        this.pin = String.format("%04d", (int) (Math.random() * 10000));
        if (!bike.getState().reserveBike()) {
            throw new IllegalStateException("Bike cannot be reserved.");
        }
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.timer = new ReservationTimer(this);
        this.timer.start();
    }

    public void cancelReservation() {
        if (this.status == ReservationStatus.ACTIVE) {
            this.status = ReservationStatus.CANCELLED;
            this.bike.getState().returnBike(this.bike.getDock());
            this.timer.interrupt();
        }
    }

    class ReservationTimer extends Thread {
        private final Reservation reservation;

        public ReservationTimer(Reservation reservation) {
            this.reservation = reservation;
        }

        @Override
        public void run() {
            reservation.expiresAt = reservation.createdAt.plus(java.time.Duration.ofMinutes(5));
            reservation.bike.setReservationExpiry(reservation.expiresAt);
            try {
                Thread.sleep(5 * 60 * 1000); // 5 minutes
                if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                    reservation.setStatus(ReservationStatus.EXPIRED);
                    reservation.bike.getState().returnBike(reservation.bike.getDock());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
