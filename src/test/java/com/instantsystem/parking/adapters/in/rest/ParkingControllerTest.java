package com.instantsystem.parking.adapters.in.rest;

import com.instantsystem.parking.config.ParkingProperties;
import com.instantsystem.parking.domain.exception.ParkingProviderException;
import com.instantsystem.parking.domain.exception.UnknownCityException;
import com.instantsystem.parking.domain.models.*;
import com.instantsystem.parking.domain.port.in.FindNearbyParkingsUseCase;
import com.instantsystem.parking.domain.port.in.NearbyParkingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de l'adaptateur REST seul : le use case est mocké, on vérifie
 * le contrat HTTP (paramètres, codes, forme du JSON).
 */
@WebMvcTest(ParkingController.class)
@Import(ParkingControllerTest.TestConfig.class)
class ParkingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FindNearbyParkingsUseCase useCase;

    static class TestConfig {
        @Bean
        ParkingProperties parkingProperties() {
            return new ParkingProperties(1000, 20, Duration.ofSeconds(30),
                    Duration.ofSeconds(1), Duration.ofSeconds(1), List.of());
        }
    }

    private static final City POITIERS = new City("poitiers", "Grand Poitiers", new Zone(46.52, 46.64, 0.27, 0.42));
    private static Instant now = Instant.now();

    private static NearbyParkingResult sample() {
        Parking parking = new Parking("3", "THEATRE", 320, 37,
                new Geopoint(46.58383455409422, 0.33779491061805567),
                now);

        return new NearbyParkingResult(POITIERS, List.of(new NearbyParking(parking, 146.4)));
    }

    @Test
    void returnsParkingsAsStableContract() throws Exception {
        when(useCase.findNearbyParkings(any())).thenReturn(sample());

        mockMvc.perform(get("/api/v1/parkings").param("latitude", "46.5802").param("longitude", "0.3404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city.id").value("poitiers"))
                .andExpect(jsonPath("$.city.name").value("Grand Poitiers"))
                .andExpect(jsonPath("$.parkings[0].id").value("3"))
                .andExpect(jsonPath("$.parkings[0].name").value("THEATRE"))
                .andExpect(jsonPath("$.parkings[0].capacity").value(320))
                .andExpect(jsonPath("$.parkings[0].availablePlaces").value(37))
                .andExpect(jsonPath("$.parkings[0].distance").value(146))
                .andExpect(jsonPath("$.parkings[0].latitude").value(46.58383455409422))
                .andExpect(jsonPath("$.parkings[0].updatedAt").value(now.toString()));
    }

    @Test
    void rejectsMissingLatitude() throws Exception {
        mockMvc.perform(get("/api/v1/parkings").param("longitude", "0.34"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOutOfRangeLatitude() throws Exception {
        mockMvc.perform(get("/api/v1/parkings").param("latitude", "95").param("longitude", "0.34"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenNoCityMatches() throws Exception {
        when(useCase.findNearbyParkings(any())).thenThrow(new UnknownCityException("Aucune ville couverte"));

        mockMvc.perform(get("/api/v1/parkings").param("latitude", "45").param("longitude", "-10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Aucune ville couverte"));
    }

    @Test
    void returns503WhenProviderIsDown() throws Exception {
        when(useCase.findNearbyParkings(any())).thenThrow(new ParkingProviderException("down", null));

        mockMvc.perform(get("/api/v1/parkings").param("latitude", "46.58").param("longitude", "0.34"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").exists());
    }
}