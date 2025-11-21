package com.acme.bms.domain.billing;

import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;

public interface BillBuilder {
    void createBill(Trip trip);
    void calculateBaseCost();
    void addUsageCost();
    void addElectricCharge();
    void applyDiscount();
    void applyTierDiscount();
    void applyFlexDollar();
    Bill getBill();
}
