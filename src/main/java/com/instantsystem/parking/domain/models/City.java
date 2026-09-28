package com.instantsystem.parking.domain.models;

import java.util.Objects;

/**
 * Une ville servie par l'application, avec sa zone géographique.
 * @param id identifiant unique de la ville
 * @param name nom de la ville
 * @param zone périmètre géographique de la ville
 */
public record City(String id, String name, Zone zone) {

    public City {
        Objects.requireNonNull(zone, "zone");
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id ne peut pas être null ou vide");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name ne peut pas être null ou vide");
        }
    }
}
