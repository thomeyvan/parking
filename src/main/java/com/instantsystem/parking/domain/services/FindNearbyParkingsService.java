package com.instantsystem.parking.domain.services;

import com.instantsystem.parking.domain.exception.UnknownCityException;
import com.instantsystem.parking.domain.models.City;
import com.instantsystem.parking.domain.models.NearbyParking;
import com.instantsystem.parking.domain.port.in.FindNearbyParkingsUseCase;
import com.instantsystem.parking.domain.port.in.NearbyParkingQuery;
import com.instantsystem.parking.domain.port.in.NearbyParkingResult;
import com.instantsystem.parking.domain.port.out.CityCatalog;
import com.instantsystem.parking.domain.port.out.ParkingProvider;

import java.util.Comparator;
import java.util.List;

public class FindNearbyParkingsService implements FindNearbyParkingsUseCase {

    private final CityCatalog cityCatalog;
    private final ParkingProvider parkingProvider;

    public FindNearbyParkingsService(CityCatalog cityCatalog, ParkingProvider parkingProvider) {
        this.cityCatalog = cityCatalog;
        this.parkingProvider = parkingProvider;
    }

    @Override
    public NearbyParkingResult findNearbyParkings(NearbyParkingQuery query) {
        // Recherche de la ville correspondant à la position d'origine
        City city = cityCatalog.findCity(query.origin())
                .orElseThrow(() -> new UnknownCityException("Aucune ville ne correspond à la position " + query.origin()));

        /**
         * Récupération de tous les parkings de la ville, calcul de la distance par rapport à l'origine,
         * filtrage des parkings dans le rayon demandé, tri par distance et limitation du nombre de résultats.
         */
        List<NearbyParking> parkings = parkingProvider.findAll(city).stream()
                .map(parking -> new NearbyParking(parking, parking.position().distanceToOtherPoint(query.origin())))
                .filter(nearbyParking -> nearbyParking.distance() <= query.radius())
                .sorted(Comparator.comparingDouble(NearbyParking::distance))
                .limit(query.limit())
                .toList();

        return new NearbyParkingResult(city, parkings);
    }
}
