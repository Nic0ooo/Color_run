package fr.esgi.color_run.service.impl.search;

import fr.esgi.color_run.service.CourseSearchStrategy;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.GeocodingService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory pour créer et sélectionner la stratégie de recherche appropriée
 */
public class CourseSearchStrategyFactory {

    private final List<CourseSearchStrategy> strategies = new ArrayList<>();

    public CourseSearchStrategyFactory(CourseService courseService, GeocodingService geocodingService) {
        // Initialiser les stratégies de recherche disponibles
        strategies.add(new ProximitySearchStrategyImpl(courseService, geocodingService)); // Stratégie de recherche par proximité
        strategies.add(new GenericSearchStrategyImpl(courseService)); // Stratégie par défaut en dernier
    }

    /**
     * Obtient la première stratégie qui peut gérer la requête
     * @param request La requête HTTP
     * @return La stratégie de recherche appropriée
     */
    public CourseSearchStrategy getStrategy(HttpServletRequest request) {
        for (CourseSearchStrategy strategy : strategies) {
            if (strategy.canHandle(request)) {
                return strategy;
            }
        }

        // Si aucune stratégie ne peut gérer la requête, utiliser la dernière (stratégie générique)
        return strategies.get(strategies.size() - 1);
    }
}