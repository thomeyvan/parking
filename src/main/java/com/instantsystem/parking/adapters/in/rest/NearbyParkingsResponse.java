package com.instantsystem.parking.adapters.in.rest;


import com.instantsystem.parking.domain.port.in.NearbyParkingResult;

import java.util.List;

/**
 * Contrat exposé à l'application mobile.
 */
public record NearbyParkingsResponse(CityResponse city, List<ParkingResponse> parkings) {

    public record CityResponse(String id, String name) {
    }

    static NearbyParkingsResponse from(NearbyParkingResult result) {
        return new NearbyParkingsResponse(
            new CityResponse(result.city().id(), result.city().name()),
            result.parkings().stream().map(ParkingResponse::from).toList()
        );
    }
}
