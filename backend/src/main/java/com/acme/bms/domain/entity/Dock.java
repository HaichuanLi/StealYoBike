package com.acme.bms.domain.entity;

import com.acme.bms.IDPinGenerator;
import com.acme.bms.domain.entity.Status.BikeState.BikeState;
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
    private String id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private DockingStation station;

    @Enumerated(EnumType.STRING)
    private DockStatus status;

    private int slotIndex; // position inside the station

    @OneToOne(mappedBy = "dock", cascade = CascadeType.ALL)
    private Bike bike;

    
    public Dock(DockStatus status) {
        this.id = IDPinGenerator.generateID();
        this.status =status;
        

    }
    private String getBikeState(){
        return bike.getState();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DockingStation getStation() {
        return station;
    }

    public void setStation(DockingStation station) {
        this.station = station;
    }

    public DockStatus getStatus() {
        return status;
    }

    public void setStatus(DockStatus status) {
        this.status = status;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        if(bike == null){
            this.status=DockStatus.EMPTY;
        }
        this.bike = bike;
        this.status = DockStatus.OCCUPIED;
    }
    public boolean hasBike(){
        return this.bike!=null || this.status!=DockStatus.EMPTY;
    }
}
