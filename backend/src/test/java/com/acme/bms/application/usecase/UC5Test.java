package com.acme.bms.application.usecase;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Operator;


class UC5Test {

    @Test
    void rebalanceBikes_movesCorrectAmount_whenStationsHaveUnequalBikes() {
        // Arrange
        Operator operator = new Operator();
        
        // Create mock stations
        DockingStation fromStation = mock(DockingStation.class);
        DockingStation toStation = mock(DockingStation.class);
        
        // FromStation has 6 ELECTRIC bikes, ToStation has 2 ELECTRIC bikes
        // Should move (6-2)/2 = 2 bikes
        when(fromStation.getNumberOfAvailableBikes(BikeType.ELECTRIC)).thenReturn(6);
        when(toStation.getNumberOfAvailableBikes(BikeType.ELECTRIC)).thenReturn(2);
        
        // Create mock bikes and docks for moving
        Bike bike1 = createMockBike(1L);
        Bike bike2 = createMockBike(2L);
        Dock sourceDock1 = createMockDock(10L);
        Dock sourceDock2 = createMockDock(11L);
        Dock targetDock1 = createMockDock(20L);
        Dock targetDock2 = createMockDock(21L);
        
        // Setup bike-dock relationships
        when(bike1.getDock()).thenReturn(sourceDock1);
        when(bike2.getDock()).thenReturn(sourceDock2);
        
        // Mock the station methods to return bikes and docks in sequence
        when(fromStation.getFirstAvailableBike(BikeType.ELECTRIC))
            .thenReturn(bike1)
            .thenReturn(bike2);
        
        when(toStation.findEmptyDock())
            .thenReturn(targetDock1)
            .thenReturn(targetDock2);
        
        // Act
        boolean result = operator.rebalanceBikes(fromStation, toStation, BikeType.ELECTRIC);
        
        // Assert
        assertThat(result).isTrue();
        
        // Verify bikes were moved to target docks
        verify(targetDock1).setBike(bike1);
        verify(targetDock2).setBike(bike2);
        
        // Verify source docks were cleared
        verify(sourceDock1).setBike(null);
        verify(sourceDock2).setBike(null);
        
        // Verify bikes were reassigned to new docks
        verify(bike1).setDock(targetDock1);
        verify(bike2).setDock(targetDock2);
    }
    
    @Test
    void rebalanceBikes_returnsFalse_whenExceptionOccurs() {
        // Arrange
        Operator operator = new Operator();
        
        DockingStation fromStation = mock(DockingStation.class);
        DockingStation toStation = mock(DockingStation.class);
        
        // Setup stations to have different bike counts
        when(fromStation.getNumberOfAvailableBikes(BikeType.REGULAR)).thenReturn(4);
        when(toStation.getNumberOfAvailableBikes(BikeType.REGULAR)).thenReturn(0);
        
        // Mock first bike retrieval to succeed
        Bike bike1 = createMockBike(1L);
        when(fromStation.getFirstAvailableBike(BikeType.REGULAR))
            .thenReturn(bike1)
            .thenThrow(new RuntimeException("No more bikes available"));
        
        Dock sourceDock = createMockDock(10L);
        when(bike1.getDock()).thenReturn(sourceDock);
        
        Dock targetDock = createMockDock(20L);
        when(toStation.findEmptyDock()).thenReturn(targetDock);
        
        // Act
        boolean result = operator.rebalanceBikes(fromStation, toStation, BikeType.REGULAR);
        
        // Assert
        assertThat(result).isFalse();
        
        // Verify first bike was moved successfully before exception
        verify(targetDock).setBike(bike1);
        verify(sourceDock).setBike(null);
        verify(bike1).setDock(targetDock);
    }
    
    private Bike createMockBike(Long id) {
        Bike bike = mock(Bike.class);
        when(bike.getId()).thenReturn(id);
        return bike;
    }
    
    private Dock createMockDock(Long id) {
        Dock dock = mock(Dock.class);
        when(dock.getId()).thenReturn(id);
        return dock;
    }
}
