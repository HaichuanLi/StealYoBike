package com.acme.bms.domain.entity.Status.BikeState;

import com.acme.bms.domain.entity.Bike;

public abstract class BikeState {
    protected Bike bike;

    public BikeState(Bike bike) {
        this.bike = bike;
    }

    public abstract boolean reserveBike();
    public abstract boolean checkoutBike();
    public abstract boolean returnBike();
}