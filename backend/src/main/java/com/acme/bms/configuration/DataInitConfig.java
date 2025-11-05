package com.acme.bms.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!test") // Don't run data init in test profile
public class DataInitConfig {
        private final StationRepository stationRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        @PostConstruct
        public void initData() {
                log.info("Initializing database with preset data...");

                List<DockingStation> stations = new ArrayList<>();

                // Station 1: Concordia Station
                DockingStation concordiaStation = createStation(
                                "Concordia Station",
                                "1455 Blvd. De Maisonneuve Ouest, Montreal, Quebec H3G 1M8",
                                45.494833345243485,
                                -73.57790540553631,
                                20,
                                StationStatus.ACTIVE);
                populateStation(concordiaStation, 12); // 12 bikes out of 20 docks
                stations.add(concordiaStation);

                // Station 2: McGill Station
                DockingStation mcgillStation = createStation(
                                "McGill Station",
                                "845 Sherbrooke St W, Montreal, Quebec H3A 0G4",
                                45.504474,
                                -73.574215,
                                15,
                                StationStatus.ACTIVE);
                populateStation(mcgillStation, 8); // 8 bikes out of 15 docks
                stations.add(mcgillStation);

                // Station 3: Old Port Station
                DockingStation oldPortStation = createStation(
                                "Old Port Station",
                                "333 Rue de la Commune O, Montreal, Quebec H2Y 2E2",
                                45.502876,
                                -73.554331,
                                25,
                                StationStatus.ACTIVE);
                populateStation(oldPortStation, 18); // 18 bikes out of 25 docks
                stations.add(oldPortStation);

                // Station 4: Mount Royal Station (Under Maintenance)
                DockingStation mountRoyalStation = createStation(
                                "Mount Royal Station",
                                "1260 Chemin Remembrance, Montreal, Quebec H3H 1A2",
                                45.503951,
                                -73.587807,
                                10,
                                StationStatus.OUT_OF_SERVICE);
                populateStation(mountRoyalStation, 3); // Only 3 bikes, station under maintenance
                stations.add(mountRoyalStation);

                // Station 5: Jean-Talon Market Station
                DockingStation jeanTalonStation = createStation(
                                "Jean-Talon Market Station",
                                "7070 Avenue Henri-Julien, Montreal, Quebec H2S 3S3",
                                45.536298,
                                -73.614895,
                                30,
                                StationStatus.ACTIVE);
                populateStation(jeanTalonStation, 22); // 22 bikes out of 30 docks
                stations.add(jeanTalonStation);

                // Save all stations
                stationRepository.saveAll(stations);

                log.info("Database initialization complete. Created {} stations with a total of {} docks",
                                stations.size(),
                                stations.stream().mapToInt(DockingStation::getCapacity).sum());

                // Log summary
                for (DockingStation station : stations) {
                        long bikesCount = station.getDocks().stream()
                                        .filter(dock -> dock.getBike() != null)
                                        .count();
                        log.info("Station '{}': {} bikes in {} docks ({}% occupancy)",
                                        station.getName(),
                                        bikesCount,
                                        station.getCapacity(),
                                        (bikesCount * 100) / station.getCapacity());
                }

                User operator = User.builder()
                                .username("operator")
                                .address("op's address")
                                .email("op@gmail.com")
                                .passwordHash(passwordEncoder.encode("password1234"))
                                .paymentToken("op_payment_token")
                                .role(Role.OPERATOR)
                                .fullName("operatorfullname")
                                .build();
                // Only create the default operator if it doesn't already exist
                if (!userRepository.existsByEmail(operator.getEmail())
                                && !userRepository.existsByUsername(operator.getUsername())) {
                        userRepository.save(operator);
                        log.info("Created default operator user with username '{}'",
                                        operator.getUsername());
                } else {
                        log.info("Default operator user already exists (email or username), skipping creation");
                }

        }

        private DockingStation createStation(String name, String address, double lat, double lon,
                        int capacity, StationStatus status) {
                return DockingStation.builder()
                                .name(name)
                                .streetAddress(address)
                                .latitude(lat)
                                .longitude(lon)
                                .capacity(capacity)
                                .status(status)
                                .expiresAfterMinutes(30)
                                .docks(new ArrayList<>())
                                .build();
        }

        private void populateStation(DockingStation station, int numberOfBikes) {
                // Create all docks for the station
                for (int i = 1; i <= station.getCapacity(); i++) {
                        Dock dock = Dock.builder()
                                        .station(station)
                                        .slotIndex(i)
                                        .status(DockStatus.EMPTY)
                                        .build();
                        station.getDocks().add(dock);
                }

                for (int i = 0; i < numberOfBikes && i < station.getCapacity(); i++) {
                        Dock dock = station.getDocks().get(i);

                        // Mix of regular and electric bikes
                        BikeType bikeType = (i % 3 == 0) ? BikeType.ELECTRIC : BikeType.REGULAR;

                        Bike bike = Bike.builder()
                                        .type(bikeType)
                                        .dock(dock)
                                        .build();

                        dock.setBike(bike);
                        dock.setStatus(DockStatus.OCCUPIED);
                }
        }
}
