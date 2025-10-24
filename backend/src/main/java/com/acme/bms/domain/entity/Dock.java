package com.acme.bms.domain.entity;

import com.acme.bms.domain.entity.Status.DockStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "docks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @Enumerated(EnumType.STRING)
    private DockStatus status;

    private int slotIndex; // position inside the station

    @OneToOne(mappedBy = "dock", cascade = CascadeType.ALL)
    private Bike bike;
}
