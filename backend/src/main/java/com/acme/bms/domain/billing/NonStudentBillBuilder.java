package com.acme.bms.domain.billing;

import java.time.Duration;

import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;

import lombok.Getter;

@Getter
public class NonStudentBillBuilder implements BillBuilder{
    private Bill bill;
    private double baseFee = 2.50;
    private double usageFee = 0.15;
    private double eBikeFeeMultiplier = 1.5;


    @Override
    public void createBill(Trip trip) {
        bill = new Bill();
        bill.setTrip(trip);
    }

    @Override
    public void calculateBaseCost() {
        bill.setTotalAmount(bill.getTotalAmount() + baseFee);
    }

    @Override
    public void addUsageCost() {
        bill.setTotalAmount(bill.getTotalAmount() + (Duration.between(bill.getTrip().getStartTime(),bill.getTrip().getEndTime()).toMinutes()) * usageFee);
    }

    @Override
    public void addElectricCharge() {
        if(bill.getTrip().getBike().getType() == BikeType.ELECTRIC){
            bill.setTotalAmount(bill.getTotalAmount() * eBikeFeeMultiplier);
        }
        
    }

    @Override
    public void applyDiscount() {
        //no discount for now
    }
}
