package com.acme.bms.domain.entity;

import com.acme.bms.domain.entity.Status.DockStatus;

import jakarta.persistence.*;
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
    private DockingStation station;

    @Enumerated(EnumType.STRING)
    private DockStatus status;

    private int slotIndex; // position inside the station

    @OneToOne(mappedBy = "dock", cascade = CascadeType.ALL)
    private Bike bike;

    @Enumerated(EnumType.STRING)
    @Column(name = "supported_type", nullable = true)
    private BikeType supportedType;

    public boolean accepts(BikeType type) {
        return supportedType == null || supportedType == type;
    }
}
