package com.instantsystem.parking.adapters.out.portail;

import com.instantsystem.parking.domain.models.Geopoint;
import com.instantsystem.parking.domain.models.Parking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Traduit un enregistrement source en Parking du domaine.
 * Retourne Optional.empty() quand l'enregistrement est inexploitable
 */
public class OpenDataParkingMapper {
    private static final Logger log = LoggerFactory.getLogger(OpenDataParkingMapper.class);

    Optional<Parking> toDomain(OpenDataParkingResponse.Record record) {
        if (record.id() == null || record.nom() == null || record.capacite() == null || record.places() == null) {
            log.warn("Parking ignoré, champs obligatoires manquants : {}", record);
            return Optional.empty();
        }

        Optional<Geopoint> position = parsePosition(record.geoPoint());
        if (position.isEmpty()) {
            log.warn("Parking ignoré, pas de coordonnées : id={} nom={}", record.id(), record.nom());
            return Optional.empty();
        }

        return Optional.of(new Parking(
                String.valueOf(record.id()),
                record.nom().trim(),
                record.capacite(),
                Math.min(record.places(), record.capacite()),
                position.get(),
                record.derniereMiseAJour())
        );
    }

    private Optional<Geopoint> parsePosition(String geoPoint) {
        if (geoPoint == null || geoPoint.isBlank()) {
            return Optional.empty();
        }
        String[] parts = geoPoint.split(",");
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Geopoint(
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim())));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
