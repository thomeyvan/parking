package com.instantsystem.parking.domain.port.in;

import com.instantsystem.parking.domain.exception.UnknownCityException;

/**
 * Port entrant : ce que l'application offre aux adaptateurs.
 */
public interface FindNearbyParkingsUseCase {

    /**
     * Retrouve la ville (déduit de la position), puis renvoie
     * ses parkings dans le rayon demandé, triés du plus proche au plus éloigné.
     * @param query les critères de recherche.
     * @return le résultat de la recherche.
     * @throws UnknownCityException si aucune ville ne correspond.
     */
    NearbyParkingResult findNearbyParkings(NearbyParkingQuery query);
}
