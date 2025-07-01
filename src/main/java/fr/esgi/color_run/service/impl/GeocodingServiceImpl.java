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
        System.out.println("=== GEOCODING SERVICE ===");
        System.out.println("Recherche coordonnées pour le code postal: '" + postalCode + "'");

        // Vérifier si le code postal est déjà en cache
        if (coordinatesCache.containsKey(postalCode)) {
            GeoLocation cachedLocation = coordinatesCache.get(postalCode);
            System.out.println("Coordonnées trouvées en cache: " + cachedLocation.getLatitude() + ", " + cachedLocation.getLongitude());
            return cachedLocation;
        }

        try {
            // URL de l'API de géocodage avec plus de précision
            String urlStr = "https://api-adresse.data.gouv.fr/search/?q=" + postalCode + "&type=municipality&limit=1";
            System.out.println("URL de l'API: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            System.out.println("Code de réponse API: " + responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                System.out.println("Réponse JSON de l'API: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...");

                // Parser la réponse JSON
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray features = jsonObject.getJSONArray("features");

                if (features.length() > 0) {
                    JSONObject feature = features.getJSONObject(0);
                    JSONArray coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates");

                    // L'API renvoie [longitude, latitude]
                    double longitude = coordinates.getDouble(0);
                    double latitude = coordinates.getDouble(1);

                    System.out.println("Coordonnées reçues de l'API: longitude=" + longitude + ", latitude=" + latitude);

                    // Vérification de cohérence pour la France métropolitaine
                    if (latitude >= 41.0 && latitude <= 51.5 && longitude >= -5.0 && longitude <= 10.0) {
                        GeoLocation location = new GeoLocation(latitude, longitude);

                        // Stocker en cache pour les futures demandes
                        coordinatesCache.put(postalCode, location);
                        System.out.println("Coordonnées stockées en cache pour " + postalCode + ": " + latitude + ", " + longitude);

                        return location;
                    } else {
                        System.err.println("Coordonnées hors limites France: lat=" + latitude + ", lon=" + longitude);
                    }
                } else {
                    System.err.println("Aucun résultat trouvé dans la réponse API");
                }
            } else {
                System.err.println("Erreur HTTP: " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion du code postal en coordonnées: " + e.getMessage());
            e.printStackTrace();
        }

        // En cas d'erreur, utiliser des coordonnées par défaut (Paris) ET l'indiquer clairement
        System.out.println("ATTENTION: Utilisation des coordonnées par défaut (Paris) pour le code postal " + postalCode);
        GeoLocation defaultLocation = new GeoLocation(48.8566, 2.3522);

        // NE PAS mettre en cache les coordonnées par défaut pour éviter les erreurs persistantes
        System.out.println("=== FIN GEOCODING SERVICE ===");

        return defaultLocation;
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

    @Override
    public String getAddressFromCoordinates(double latitude, double longitude) {
        try {
            // Utiliser la même API française mais en reverse
            String urlStr = "https://api-adresse.data.gouv.fr/reverse/?lon=" + longitude + "&lat=" + latitude + "&limit=1";

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

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
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray features = jsonObject.getJSONArray("features");

                if (features.length() > 0) {
                    JSONObject feature = features.getJSONObject(0);
                    JSONObject properties = feature.getJSONObject("properties");

                    // Récupérer les éléments d'adresse
                    String name = properties.optString("name", "");
                    String postcode = properties.optString("postcode", "");
                    String city = properties.optString("city", "");

                    // Construire l'adresse complète
                    if (!name.isEmpty()) {
                        return name + ", " + postcode + " " + city;
                    } else {
                        return postcode + " " + city;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du reverse geocoding: " + e.getMessage());
        }

        // Retourner null si pas trouvé
        return null;
    }
}