package fr.esgi.color_run.service.impl.search;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.service.CourseSearchStrategy;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.GeocodingService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stratégie de recherche par proximité géographique (géolocalisation)
 */
public class ProximitySearchStrategyImpl implements CourseSearchStrategy {

    private final CourseService courseService;
    private final GeocodingService geocodingService;

    public ProximitySearchStrategyImpl(CourseService courseService, GeocodingService geocodingService) {
        this.courseService = courseService;
        this.geocodingService = geocodingService;
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {
        String searchType = request.getParameter("searchType");
        return "proximity".equals(searchType);
    }

    @Override
    public List<Course> search(HttpServletRequest request) {
        try {
            double latitude = Double.parseDouble(request.getParameter("latitude"));
            double longitude = Double.parseDouble(request.getParameter("longitude"));

            // Utiliser un rayon par défaut de 50km si non spécifié
            int radius = 50;
            try {
                String radiusParam = request.getParameter("radius");
                if (radiusParam != null && !radiusParam.isEmpty()) {
                    radius = Integer.parseInt(radiusParam);
                }
            } catch (NumberFormatException e) {
                System.err.println("Erreur de conversion du rayon, utilisation de la valeur par défaut: " + e.getMessage());
            }

            String dateFilter = request.getParameter("dateFilter");

            // Log pour debug
            System.out.println("ProximitySearchStrategyImpl - latitude: " + latitude);
            System.out.println("ProximitySearchStrategyImpl - longitude: " + longitude);
            System.out.println("ProximitySearchStrategyImpl - radius: " + radius);
            System.out.println("ProximitySearchStrategyImpl - dateFilter: " + dateFilter);

            // Déléguer au service qui utilise le repository
            return courseService.findCoursesByProximity(latitude, longitude, radius, dateFilter);
        } catch (NumberFormatException e) {
            // Log l'erreur et retourner une liste vide
            System.err.println("Erreur lors de la conversion des paramètres de recherche par proximité: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getContextParameters(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        try {
            double latitude = Double.parseDouble(request.getParameter("latitude"));
            double longitude = Double.parseDouble(request.getParameter("longitude"));

            int radius = 50; // Valeur par défaut
            try {
                String radiusParam = request.getParameter("radius");
                if (radiusParam != null && !radiusParam.isEmpty()) {
                    radius = Integer.parseInt(radiusParam);
                }
            } catch (NumberFormatException e) {
                // Ignorer et garder la valeur par défaut
            }

            String dateFilter = request.getParameter("dateFilter");

            params.put("searchRadius", radius);
            params.put("searchLatitude", latitude);
            params.put("searchLongitude", longitude);
            params.put("searchDateFilter", dateFilter);
            params.put("isGeolocationSearch", true);
        } catch (NumberFormatException e) {
            // Ignorer l'erreur, les paramètres ne seront pas ajoutés
        }
        return params;
    }
}