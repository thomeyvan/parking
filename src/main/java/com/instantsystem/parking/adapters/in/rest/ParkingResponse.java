package com.instantsystem.parking.adapters.in.rest;


import com.instantsystem.parking.domain.models.NearbyParking;
import com.instantsystem.parking.domain.models.Parking;

import java.time.Instant;

/**
 * Contrat exposé à l'application mobile.
 */
public record ParkingResponse(
        String id,
        String name,
        int capacity,
        int availablePlaces,
        int distance,
        double latitude,
        double longitude,
        Instant updatedAt) {

    static ParkingResponse from(NearbyParking nearby) {
        Parking parking = nearby.parking();

        return new ParkingResponse(
                parking.id(),
                parking.name(),
                parking.capacity(),
                parking.availablePlaces(),
                (int) Math.round(nearby.distance()),
                parking.position().latitude(),
                parking.position().longitude(),
                parking.updatedAt());
    }
}
