package com.instantsystem.parking.domain.models;

/**
 * Parking à proximité d'un point géographique, avec la distance correspondante.
 *
 * @param parking le parking
 * @param distance la distance entre le point géographique et le parking, en mètres
 */
public record NearbyParking(Parking parking, double distance) {
}
