package com.instantsystem.parking.domain.models;

/**
 * Position géographique exprimée en latitude et longitude
 * <p>Vérifie que la latitude est comprise entre -90 et 90 et que la longitude est comprise entre -180 et 180</p>
 * <p>Permet de calculer la distance en mètres entre deux points</p>
 * @param latitude représente la latitude du point géographique, doit être comprise entre -90 et 90 degrés
 * @param longitude représente la longitude du point géographique, doit être comprise entre -180 et 180 degrés
 */
public record Geopoint(double latitude, double longitude) {
    // Rayon moyen de la Terre en mètres
    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    public Geopoint {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude hors bornes : " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude hors bornes : " + longitude);
        }
    }

    /**
     * Permet de calculer la distance en mètres entre deux points géographiques
     * @param otherPoint: le point géographique avec lequel on veut calculer la distance
     * @return la distance en mètres entre les deux points géographiques
     */
    public double distanceToOtherPoint(Geopoint otherPoint) {
        double dLatitude = Math.toRadians(otherPoint.latitude - latitude);
        double dLongitude = Math.toRadians(otherPoint.longitude - longitude);
        double pLatitude = Math.toRadians(latitude);
        double oLatitude = Math.toRadians(otherPoint.latitude);

        double haversine = (Math.sin(dLatitude / 2) * Math.sin(dLatitude / 2))
                + (Math.cos(pLatitude) * Math.cos(oLatitude) * Math.sin(dLongitude / 2) * Math.sin(dLongitude / 2));
        double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

        return EARTH_RADIUS_METERS * centralAngle;
    }
}
