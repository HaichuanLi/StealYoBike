package com.acme.bms.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pricing_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 32)
    private PricingPlanType type;

    @Column(nullable = false)
    private double baseFee;

    @Column(nullable = false)
    private double perMinuteRate;

    @Column
    private Double surcharge; // null for STANDARD, used for EBIKE

    // helpers
    @Override
    public String toString() {
        return String.format("PricingPlan{id=%d, type=%s, baseFee=%.2f, perMinuteRate=%.2f, surcharge=%.2f}",
                id, type, baseFee, perMinuteRate, surcharge != null ? surcharge : 0.0);
    }
}
