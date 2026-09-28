package com.instantsystem.parking.adapters.out;

import com.instantsystem.parking.domain.models.Parking;

import java.util.List;

/**
 * Contrat interne aux adaptateurs : une source de données déjà liée à une
 * ville (elle connaît son URL).
 */
public interface ParkingSource {

    List<Parking> findAll();
}
