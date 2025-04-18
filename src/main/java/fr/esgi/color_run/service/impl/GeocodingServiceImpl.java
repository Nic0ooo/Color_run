package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.GeoLocation;
import fr.esgi.color_run.service.GeocodingService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;


public class GeocodingServiceImpl implements GeocodingService {
    private final Map<String, GeoLocation> coordinatesCache = new HashMap<>();

    @Override
    public GeoLocation getCoordinatesFromPostalCode(String postalCode) {
        // vérfiier si le code postal est déjà en cache
        if (coordinatesCache.containsKey(postalCode)) {
            return coordinatesCache.get(postalCode);
        }

        try {
            // URL de l'API de géocodage
            String urlStr = "https://api-adresse.data.gouv.fr/search/?q=" + postalCode + "&limit=1";
            URL url = new URL(urlStr);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parser la réponse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray features = jsonResponse.getJSONArray("features");

                if (features.length() > 0) {
                    JSONObject feature = features.getJSONObject(0);
                    JSONArray coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates");

                    // L'API renvoie [longitude, latitude]
                    double longitude = coordinates.getDouble(0);
                    double latitude = coordinates.getDouble(1);

                    GeoLocation location = new GeoLocation(latitude, longitude);

                    // Stocker en cache pour les futures demandes
                    coordinatesCache.put(postalCode, location);

                    return location;
                }
            }

            // En cas d'erreur ou si aucune donnée n'est trouvée, utiliser des coordonnées par défaut (Paris)
            return new GeoLocation(48.8566, 2.3522);

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion du code postal en coordonnées: " + e.getMessage());
            // En cas d'erreur, utiliser des coordonnées par défaut (Paris)
            return new GeoLocation(48.8566, 2.3522);
        }

    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}