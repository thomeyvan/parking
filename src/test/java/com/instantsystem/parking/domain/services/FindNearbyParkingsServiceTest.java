package com.instantsystem.parking.domain.services;

import com.instantsystem.parking.domain.exception.UnknownCityException;
import com.instantsystem.parking.domain.models.City;
import com.instantsystem.parking.domain.models.Geopoint;
import com.instantsystem.parking.domain.models.Parking;
import com.instantsystem.parking.domain.models.Zone;
import com.instantsystem.parking.domain.port.in.NearbyParkingQuery;
import com.instantsystem.parking.domain.port.in.NearbyParkingResult;
import com.instantsystem.parking.domain.port.out.CityCatalog;
import com.instantsystem.parking.domain.port.out.ParkingProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FindNearbyParkingsServiceTest {

    private static final Zone ZONE_POITIERS = new Zone(46.3051556, 46.7852034, -0.0471733, 0.7745855);
    private static final Zone ZONE_NANTES = new Zone(47.1805856, 47.2958583, -1.6418115, -1.4788443);

    private static final City POITIERS = new City("poitiers", "Poitiers", ZONE_POITIERS);
    private static final City NANTES = new City("nantes", "Nantes", ZONE_NANTES);

    private static final Geopoint CENTRE_POITIERS = new Geopoint(46.5800, 0.3400);
    private static final Geopoint CENTRE_NANTES = new Geopoint(47.2184, -1.5536);
    private static final Geopoint ATLANTIQUE = new Geopoint(45.0, -10.0);

    private static Parking parking(String id, double lat, double lon) {
        return new Parking(id, "Parking " + id, 100, 50, new Geopoint(lat, lon), Instant.now());
    }

    /** Fake du port CityCatalog */
    private final CityCatalog cityCatalog = position -> Stream.of(POITIERS, NANTES)
            .filter(city -> city.zone().contains(position))
            .findFirst();

    /**
     * Fake du port ParkingProvider : route sur l'id de la ville.
     */
    private final ParkingProvider parkingProvider = city -> switch (city.id()) {
        case "poitiers" -> List.of(
                parking("far", 46.5900, 0.3400),
                parking("near", 46.5810, 0.3400),
                parking("here", 46.5800, 0.3400));
        case "nantes" -> List.of(parking("commerce", 47.2135, -1.5560));
        default -> List.of();
    };

    private final FindNearbyParkingsService service = new FindNearbyParkingsService(cityCatalog, parkingProvider);

    @Test
    void findCityFromPositionAndSortsByDistance() {
        NearbyParkingResult result = service.findNearbyParkings(new NearbyParkingQuery(CENTRE_POITIERS, 5_000, 10));

        assertThat(result.city()).isEqualTo(POITIERS);
        assertThat(result.parkings().size()).isEqualTo(3);
        assertThat(result.parkings()).extracting(n -> n.parking().id()).containsExactly("here", "near", "far");
        assertThat(result.parkings().get(0).distance()).isZero();
    }

    @Test
    void findTheRightCity() {
        NearbyParkingResult result = service.findNearbyParkings(new NearbyParkingQuery(CENTRE_NANTES, 5_000, 10));

        assertThat(result.city()).isEqualTo(NANTES);
        assertThat(result.parkings().size()).isEqualTo(1);
        assertThat(result.parkings()).extracting(n -> n.parking().id()).containsExactly("commerce");
    }

    @Test
    void filtersOutParkingsBeyondRadius() {
        NearbyParkingResult result = service.findNearbyParkings(new NearbyParkingQuery(CENTRE_POITIERS, 500, 10));

        assertThat(result.parkings().size()).isEqualTo(2);
        assertThat(result.parkings()).extracting(n -> n.parking().id()).containsExactly("here", "near");
    }

    @Test
    void honoursLimit() {
        NearbyParkingResult result = service.findNearbyParkings(new NearbyParkingQuery(CENTRE_POITIERS, 5_000, 1));

        assertThat(result.parkings().size()).isEqualTo(1);
        assertThat(result.parkings()).extracting(n -> n.parking().id()).containsExactly("here");
    }

    @Test
    void failsWhenNoCityCoversThePosition() {
        assertThatThrownBy(() -> service.findNearbyParkings(new NearbyParkingQuery(ATLANTIQUE, 5_000, 10)))
                .isInstanceOf(UnknownCityException.class);
    }
}