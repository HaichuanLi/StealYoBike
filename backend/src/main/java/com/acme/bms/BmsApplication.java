package com.acme.bms;

import java.util.ArrayList;

import javax.print.Doc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Operator;
import com.acme.bms.domain.entity.Status.BikeState.AvailableState;
import com.acme.bms.domain.entity.Status.DockStatus;

@SpringBootApplication
public class BmsApplication {
    public static void main(String[] args) {
        testMoveBikes();
        
        SpringApplication.run(BmsApplication.class, args);
    
    
    }

    public static void testMoveBikes(){
        DockingStation ds = new DockingStation((long) 123123.0, "Main Station", "123 Main St", 12,1,10,new ArrayList<>());
        DockingStation ds2 = new DockingStation((long) 123123.0, "2 Station", "123 Main St", 12,1,10,new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            ds.getDocks().add(new com.acme.bms.domain.entity.Dock(DockStatus.EMPTY));
            ds2.getDocks().add(new com.acme.bms.domain.entity.Dock(DockStatus.EMPTY));
        }
        for(Dock dock : ds.getDocks()){
            com.acme.bms.domain.entity.Bike bike = new com.acme.bms.domain.entity.Bike(BikeType.ELECTRIC);
            bike.changeState(new AvailableState(bike));
            dock.setBike(bike);
            bike.setDock(dock);
        }
        Operator operator = new Operator();
        operator.rebalanceBikes(ds, ds2);   
    }
}
