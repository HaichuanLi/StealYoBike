package com.acme.bms.domain.entity.Status.BikeStrategy;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;

public abstract class BikeState {
    Bike bike;

    BikeState(Bike bike) {
        this.bike = bike;
    }

    public abstract boolean reserveBike();

    public abstract boolean checkoutBike();

    public abstract boolean returnBike(Dock dock);

    public abstract String toString();
}
