package com.acme.bms.domain.billing;

import java.time.Duration;

import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Plan;
import com.acme.bms.domain.entity.Tier;
import com.acme.bms.domain.entity.User;

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
        bill.setBaseFee(baseFee);
        bill.setTotalAmount(bill.getTotalAmount() + baseFee);
    }

    @Override
    public void addUsageCost() {
        long minutes = Duration.between(bill.getTrip().getStartTime(), bill.getTrip().getEndTime()).toMinutes();
        double usage = minutes * usageFee;

        User rider = bill.getTrip().getRider();
        Plan plan = rider != null ? rider.getPlan() : Plan.PAYPERRIDE;
        if (plan == Plan.MONTHLY) {
            if (minutes <= 30){
                usage = 0;
            }
            else{
                usage = minutes - 30 * baseFee;
            }
        } else if (plan == Plan.ANNUAL) {
            if (minutes <= 45){
                usage = 0;
            }
            else{
                usage = minutes - 30 * baseFee;
            }
        }

        bill.setUsageCost(usage);
        bill.setTotalAmount(bill.getTotalAmount() + usage);
    }

    @Override
    public void addElectricCharge() {
        if (bill.getTrip() != null && bill.getTrip().getBike() != null && bill.getTrip().getBike().getType() == BikeType.ELECTRIC) {
            User rider = bill.getTrip().getRider();
            Plan plan = rider != null ? rider.getPlan() : Plan.PAYPERRIDE;

            double multiplier = eBikeFeeMultiplier;
            if (plan == Plan.MONTHLY) {
                multiplier = 1 + (eBikeFeeMultiplier - 1) * 0.5;
            } else if (plan == Plan.ANNUAL) {
                multiplier = 1.0;
            }

            double electric = bill.getTotalAmount() * (multiplier - 1);
            bill.setElectricCharge(electric);
            bill.setTotalAmount(bill.getTotalAmount() * multiplier);
        } else {
            bill.setElectricCharge(0);
        }
    }

    @Override
    public void applyDiscount() {
        // no discount for now
    }

    @Override
    public void applyTierDiscount() {
        User rider = bill.getTrip().getRider();
        if (rider == null) {
            return;
        }
        
        double tierDiscountPercentage = 0.0;
        switch (rider.getTier()) {
            case BRONZE:
                tierDiscountPercentage = 0.05; // 5% discount
                break;
            case SILVER:
                tierDiscountPercentage = 0.10; // 10% discount
                break;
            case GOLD:
                tierDiscountPercentage = 0.15; // 15% discount
                break;
            default:
                tierDiscountPercentage = 0.0; // No discount for REGULAR tier
                break;
        }
        
        if (tierDiscountPercentage > 0) {
            double tierDiscount = bill.getTotalAmount() * tierDiscountPercentage;
            bill.setTierDiscountAmount(tierDiscount);
            bill.setTotalAmount(bill.getTotalAmount() * (1 - tierDiscountPercentage));
        }
    }
}
