package com.acme.bms.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.acme.bms.domain.entity.Status.DockStatus;
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
import lombok.ToString;

@Entity
@Table(name = "docking_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DockingStation {

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

    public int getNumberOfAvailableBikes() {
        int count = 0;
        for (Dock dock : docks) {
            if (dock.getBike() != null && dock.getBike().getState().equals("AVAILABLE")) {
                count++;
            }
        }
        return count;
    }

    public Bike findAvailableBike() {
        for (Dock dock : docks) {
            if (dock.getBike() != null && dock.getBike().getState().equals("AVAILABLE")) {
                return dock.getBike();
            }
        }
        return null;
    }
    public Dock findEmptyDock() {
        for (Dock dock : docks) {
            if (dock.getStatus() == DockStatus.EMPTY) {
                return dock;
            }
        }
        return null;
    }
    public String ToString(){
        String s = "";
        for (Dock dock : docks) {
            s += "Dock ID: " + dock.getId() + ", Status: " + dock.getStatus();
            if(dock.getBike() != null){
                s += ", Bike ID: " + dock.getBike().getId() + ", Bike State: " + dock.getBike().getState();
            }
            s += "\n";
        }
        return s;
    }
}
