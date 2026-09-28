package com.instantsystem.parking.domain.models;

/**
 * Périmètre (Emprise géographique) rectangulaire d'une ville.
 * @param minLatitude latitude minimale du périmètre géographique
 * @param maxLatitude latitude maximale du périmètre géographique
 * @param minLongitude longitude minimale du périmètre géographique
 * @param maxLongitude longitude maximale du périmètre géographique
 */
public record Zone(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {

    public Zone {
        if (minLatitude > maxLatitude || minLongitude > maxLongitude) {
            throw new IllegalArgumentException("Périmètre géographique invalide");
        }
    }

    /**
     * Vérifie si un point géographique est contenu dans le périmètre de la ville.
     * @param position le point géographique à vérifier
     * @return true si le point est contenu dans le périmètre, false sinon
     */
    public boolean contains(Geopoint position) {
        return position.latitude() >= minLatitude && position.latitude() <= maxLatitude
                && position.longitude() >= minLongitude && position.longitude() <= maxLongitude;
    }
}
