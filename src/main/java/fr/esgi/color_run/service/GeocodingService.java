package fr.esgi.color_run.service;

import fr.esgi.color_run.business.GeoLocation;

public interface GeocodingService {
    /**
     * Convertir le code postal en coordonnées géographiques
     */
    GeoLocation getCoordinatesFromPostalCode(String postalCode);

    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    /**
     * Convertir les coordonnées en adresse (reverse geocoding)
     */
    String getAddressFromCoordinates(double latitude, double longitude);
}