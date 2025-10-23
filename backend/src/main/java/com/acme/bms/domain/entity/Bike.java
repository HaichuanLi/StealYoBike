package com.acme.bms.domain.entity;

import com.acme.bms.domain.entity.Status.BikeStrategy.BikeState;
import com.acme.bms.domain.entity.Status.BikeStrategy.AvailableState;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bikes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BikeType type;

    private BikeState state = new AvailableState(this);

    @ManyToOne
    @JoinColumn(name = "dock_id")
    private Dock dock; // null when undocked or on trip

    private java.time.LocalDateTime reservationExpiry;
}
