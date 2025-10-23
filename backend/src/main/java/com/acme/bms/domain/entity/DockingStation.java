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
    private ArrayList<Dock> docks;

    public DockingStation(Long id, String name, String streetAddress, double latitude, double longitude, int capacity,
            ArrayList<Dock> docks) {
        this.id = id;
        this.name = name;
        this.streetAddress = streetAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.docks = docks;
        this.expiresAfterMinutes=5;
        this.status=StationStatus.ACTIVE;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }

    public int getExpiresAfterMinutes() {
        return expiresAfterMinutes;
    }

    public void setExpiresAfterMinutes(int expiresAfterMinutes) {
        this.expiresAfterMinutes = expiresAfterMinutes;
    }

    public List<Dock> getDocks() {
        return docks;
    }

    public void setDocks(ArrayList<Dock> docks) {
        this.docks =  docks;
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
