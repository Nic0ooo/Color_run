package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Course;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * Interface définisant une stratégie de recherche de courses
 */
public interface CourseSearchStrategy {
    /**
     * Verifie si cette stratégie peut gérer le type de recherche demandé
     */
    boolean canHandle(HttpServletRequest request);

    /**
     * Effectue la recherche selon al stratégie
     */
    List<Course> search(HttpServletRequest request);

    /**
     * Recupère les paramètres de contexte à utiliser dans la vue
     */
    Map<String, Object> getContextParameters(HttpServletRequest request);
}
