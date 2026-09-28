package com.instantsystem.parking.domain.port.in;

import com.instantsystem.parking.domain.models.NearbyParking;
import com.instantsystem.parking.domain.models.City;

import java.util.List;

/**
 * Résultat d'une recherche : la ville résolue et ses parkings triés par distance.
 * @param city la ville résolue
 * @param parkings la liste des parkings triés par distance
 */
public record NearbyParkingResult(City city, List<NearbyParking> parkings) {
}
