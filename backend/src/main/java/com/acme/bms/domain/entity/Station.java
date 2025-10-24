package com.acme.bms.domain.entity;

import java.util.List;

import com.acme.bms.domain.entity.Status.StationStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "docking_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String streetAddress;
    private double latitude;
    private double longitude;
    private int capacity;

    @Enumerated(EnumType.STRING)
    private StationStatus status;

    private int expiresAfterMinutes;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dock> docks;

    public Bike getFirstAvailableBike(BikeType type) {
        for (Dock dock : docks) {
            if (dock.getBike() != null && dock.getBike().getType().equals(type) &&
                    dock.getBike().getState().toString().equals("Available")) {
                return dock.getBike();
            }
        }
        return null;
    }
}
