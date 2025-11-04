package com.acme.bms.domain.billing;

import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;

public class BillBuilderDirector {
    public Bill constructBill(BillBuilder builder, Trip trip) {
        builder.createBill(trip);
        builder.calculateBaseCost();
        builder.addUsageCost();
        builder.addElectricCharge();
        builder.applyDiscount();
        return builder.getBill();
    }
}
