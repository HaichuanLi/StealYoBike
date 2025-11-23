package com.acme.bms.domain.billing;

import java.time.Duration;

import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Plan;
import com.acme.bms.domain.entity.User;

import lombok.Getter;

@Getter
public class NonStudentBillBuilder implements BillBuilder {
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
            if (minutes <= 30) {
                usage = 0;
            } else {
                usage = (minutes - 30) * usageFee;
            }
        } else if (plan == Plan.ANNUAL) {
            if (minutes <= 45) {
                usage = 0;
            } else {
                usage = (minutes - 45) * usageFee;
            }
        }

        bill.setUsageCost(usage);
        bill.setTotalAmount(bill.getTotalAmount() + usage);
    }

    @Override
    public void addElectricCharge() {
        if (bill.getTrip() != null && bill.getTrip().getBike() != null
                && bill.getTrip().getBike().getType() == BikeType.ELECTRIC) {
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
        // no non-student global discount for now
    }

    @Override
    public void applyTierDiscount() {
        User rider = bill.getTrip().getRider();
        if (rider == null) {
            return;
        }

        // Only apply tier discount when the user is "acting as rider"
        String effectiveRole = rider.getActiveRole() != null
                ? rider.getActiveRole()
                : rider.getRole().name();

        boolean isRiderContext = "RIDER".equalsIgnoreCase(effectiveRole);
        if (!isRiderContext) {
            // operator/admin in operator view → no tier discount
            return;
        }

        double tierDiscountPercentage = 0.0;
        switch (rider.getTier()) {
            case BRONZE -> tierDiscountPercentage = 0.05;
            case SILVER -> tierDiscountPercentage = 0.10;
            case GOLD   -> tierDiscountPercentage = 0.15;
            default     -> tierDiscountPercentage = 0.0;
        }

        if (tierDiscountPercentage > 0) {
            double tierDiscount = bill.getTotalAmount() * tierDiscountPercentage;
            bill.setTierDiscountAmount(tierDiscount);
            bill.setTotalAmount(bill.getTotalAmount() * (1 - tierDiscountPercentage));
        }
    }

    @Override
    public void applyFlexDollar() {
        // Skip if this bill should not use flex dollars (e.g., same trip that earned them)
        if (bill.isSkipFlexDollar()) {
            return;
        }

        User rider = bill.getTrip().getRider();
        if (rider == null || rider.getFlexDollar() <= 0) {
            return;
        }

        double flexUsed = Math.min(rider.getFlexDollar(), bill.getTotalAmount());
        bill.setFlexDollarUsed(flexUsed);
        bill.setTotalAmount(bill.getTotalAmount() - flexUsed);

        rider.setFlexDollar(rider.getFlexDollar() - flexUsed);
    }
}
