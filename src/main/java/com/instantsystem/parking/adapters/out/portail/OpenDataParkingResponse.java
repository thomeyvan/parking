package com.instantsystem.parking.adapters.out.portail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Représentation brute de la reponse de l'API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record OpenDataParkingResponse(int total, List<Record> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Record(
            @JsonProperty("Id") Integer id,
            @JsonProperty("Nom") String nom,
            @JsonProperty("Capacite") Integer capacite,
            @JsonProperty("Places") Integer places,
            @JsonProperty("infos_parkingsgeo_point") String geoPoint,
            @JsonProperty("Dernière_mise_à_jour_Base") Instant derniereMiseAJour
    ) {
    }
}
