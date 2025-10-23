package com.acme.bms.domain.entity.Status.BikeStrategy;

import com.acme.bms.domain.entity.Bike;

public abstract class BikeState {
    Bike bike;

    BikeState(Bike bike) {
        this.bike = bike;
    }

    public abstract boolean reserveBike();

    public abstract boolean checkoutBike();

    public abstract boolean returnBike();

    public abstract String toString();
}
