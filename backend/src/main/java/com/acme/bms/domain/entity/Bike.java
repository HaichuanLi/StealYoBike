package com.acme.bms.domain.entity;

import com.acme.bms.domain.entity.Status.BikeState.BikeState;
import com.acme.bms.domain.entity.Status.BikeState.AvailableState;
import com.acme.bms.domain.entity.Status.BikeStatus;

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

    @Enumerated(EnumType.STRING)
    private BikeStatus status;

    // If true, bike was forced into maintenance due to operator action (e.g.
    // station out-of-service)
    @Builder.Default
    private boolean maintenanceForced = false;

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
        // restore the transient state object from the persisted status
        if (state == null) {
            if (status == null) {
                // default to available
                status = BikeStatus.AVAILABLE;
                state = new AvailableState(this);
            } else {
                switch (status) {
                    case RESERVED:
                        state = new com.acme.bms.domain.entity.Status.BikeState.ReservedState(this);
                        break;
                    case ON_TRIP:
                        state = new com.acme.bms.domain.entity.Status.BikeState.OnTripState(this);
                        break;
                    case MAINTENANCE:
                        state = new com.acme.bms.domain.entity.Status.BikeState.MaintenanceState(this);
                        break;
                    case AVAILABLE:
                    default:
                        state = new AvailableState(this);
                        break;
                }
            }
        }
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

    public boolean sendToMaintenance() {
        ensureState();
        return state.sendToMaintenance();
    }

}
