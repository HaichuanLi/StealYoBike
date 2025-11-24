package com.acme.bms.application.service;

import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.DockingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationObserverService {
    
    private final DockingStationRepository stationRepository;
    private static final double LOW_AVAILABILITY_THRESHOLD = 10.0;
    
    @Transactional
    public String checkAndNotify(DockingStation station, Long currentUserId) {
        if (station.isLowAvailability(LOW_AVAILABILITY_THRESHOLD)) {
            log.info("Low availability detected at {}: {}%", 
                     station.getName(), 
                     station.getAvailabilityPercentage());
            
            return notifyObservers(station, currentUserId);
        }
        return null;
    }
    
    private String notifyObservers(DockingStation station, Long currentUserId) {
        String notificationMessage = null;
        
        for (User observer : station.getObservers()) {
            String message = String.format(
                "Low bike availability at %s: %.1f%% (%d/%d bikes available)",
                station.getName(),
                station.getAvailabilityPercentage(),
                station.getAllAvailableBikes(),
                station.getCapacity()
            );
            
            log.info("Notifying user {}: {}", observer.getUsername(), message);
            
            if (observer.getId().equals(currentUserId)) {
                notificationMessage = message;
            }
        }
        
        return notificationMessage;
    }
}
