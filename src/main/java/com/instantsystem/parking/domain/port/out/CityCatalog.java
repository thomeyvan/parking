package com.instantsystem.parking.domain.port.out;

import com.instantsystem.parking.domain.models.Geopoint;
import com.instantsystem.parking.domain.models.City;

import java.util.Optional;

/**
 * Port sortant : le référentiel des villes (tenants) servies
 */
public interface CityCatalog {

    /**
     * Détermine la ville correspondant à une position géographique.
     * @return la ville correspondante, ou vide si aucune ville ne correspond.
     */
    Optional<City> findCity(Geopoint position);
}
