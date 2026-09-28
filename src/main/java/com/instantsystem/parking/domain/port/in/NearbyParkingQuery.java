package com.instantsystem.parking.domain.port.in;

import com.instantsystem.parking.domain.models.Geopoint;

import java.util.Objects;

/**
 * Requête pour rechercher des parkings à proximité d'un point géographique.
 *
 * @param origin le point géographique de départ
 * @param radius le rayon de recherche en mètres
 * @param limit le nombre maximum de résultats à retourner
 */
public record NearbyParkingQuery(Geopoint origin, int radius, int limit) {

    public NearbyParkingQuery {
        Objects.requireNonNull(origin, "origin");
        if (radius <= 0) {
            throw new IllegalArgumentException("Le rayon doit être > 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("La limite doit être > 0");
        }
    }
}
