package com.instantsystem.parking.domain.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Modèle de parking exploitable par l'application, quelle que soit la ville d'origine.
 * Le modèle ne contient que ce dont l'API mobile ou web a besoin.
 * @param id identifiant unique du parking
 * @param name nom du parking
 * @param capacity capacité totale du parking
 * @param availablePlaces nombre de places disponibles dans le parking
 * @param position position géographique du parking
 * @param updatedAt date et heure de la dernière mise à jour des informations du parking
 */
public record Parking(
        String id,
        String name,
        int capacity,
        int availablePlaces,
        Geopoint position,
        Instant updatedAt
) {
    public Parking {
        Objects.requireNonNull(position, "position");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id ne peut pas être null ou vide");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name ne peut pas être null ou vide");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity ne peut pas être négatif");
        }
        if (availablePlaces < 0) {
            throw new IllegalArgumentException("availablePlaces ne peut pas être négatif");
        }
        if (availablePlaces > capacity) {
            throw new IllegalArgumentException("availablePlaces ne peut pas être supérieur à capacity");
        }
    }
}
