package com.acme.bms.domain.entity;

import com.acme.bms.domain.entity.Status.BikeState.BikeState;
import com.acme.bms.domain.entity.Status.BikeState.AvailableState;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

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
    private Long id;

    @Enumerated(EnumType.STRING)
    private BikeType type;

    @Transient
    private BikeState state;

    // One bike <-> one dock. Bike owns the FK column "dock_id".
    @OneToOne
    @JoinColumn(name = "dock_id", unique = true)
    private Dock dock; // null when undocked or on trip

    private Instant reservationExpiry;

    @PostLoad
    @PostPersist
    @PostUpdate
    private void ensureState() {
        if (state == null)
            state = new AvailableState(this);
    }

    public boolean reserveBike() {
        ensureState();
        return state.reserveBike();
    }

    public boolean checkoutBike() {
        ensureState();
        return state.checkoutBike();
    }

    public boolean returnBike(Dock dock) {
        ensureState();
        return state.returnBike(dock);
    }
}
