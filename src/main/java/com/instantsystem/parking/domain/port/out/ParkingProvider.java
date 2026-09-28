package com.instantsystem.parking.domain.port.out;

import com.instantsystem.parking.domain.exception.ParkingProviderException;
import com.instantsystem.parking.domain.models.City;
import com.instantsystem.parking.domain.models.Parking;

import java.util.List;

/**
 * Port sortant : la source de données d'une ville.
 */
public interface ParkingProvider {

    /**
     * Retourne tous les parkings connus pour une ville donnée, avec leur disponibilité du moment.
     * @throws ParkingProviderException si la source est injoignable ou inexploitable.
     */
    List<Parking> findAll(City city);
}
