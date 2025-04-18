package fr.esgi.color_run.service.impl.search;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.service.CourseSearchStrategy;
import fr.esgi.color_run.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericSearchStrategyImpl implements CourseSearchStrategy {

    private final CourseService courseService;

    public GenericSearchStrategyImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {
        // Cette stratégie est la stratégie par défaut
        return "generic".equals(request.getParameter("searchType"))
                || request.getParameter("searchType") == null;
    }

    @Override
    public List<Course> search(HttpServletRequest request) {
        // Récupérer tous les paramètres du formulaire
        String postalCode = request.getParameter("postalCode");
        String dateFilter = request.getParameter("dateFilter");
        String postalRadiusStr = request.getParameter("postalRadius");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        // Traiter le rayon
        int postalRadius = 0;
        if (postalRadiusStr != null && !postalRadiusStr.isEmpty()) {
            try {
                postalRadius = Integer.parseInt(postalRadiusStr);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de conversion du rayon: " + e.getMessage());
            }
        }

        // Traiter le code postal pour gérer les arrondissements
        if (postalCode != null && postalCode.length() == 5) {
            try {
                int cp = Integer.parseInt(postalCode);
                // Si les deux derniers chiffres sont différents de 00 et que le rayon est > 0
                // (ex: 69008 pour Lyon 8ème)
                if (cp % 100 != 0 && postalRadius > 0) {
                    // Arrondir au code postal de la ville (69000 pour Lyon)
                    postalCode = String.format("%d00", cp / 100);
                    System.out.println("Code postal d'arrondissement détecté, conversion en: " + postalCode);
                }
            } catch (NumberFormatException e) {
                // Ignorer si ce n'est pas un nombre
            }
        }

        // Logs pour debug
        System.out.println("GenericSearchStrategyImpl - postalCode: " + postalCode);
        System.out.println("GenericSearchStrategyImpl - dateFilter: " + dateFilter);
        System.out.println("GenericSearchStrategyImpl - postalRadius: " + postalRadius);
        System.out.println("GenericSearchStrategyImpl - startDate: " + startDate);
        System.out.println("GenericSearchStrategyImpl - endDate: " + endDate);

        // Si tous les paramètres sont vides, retourner toutes les courses
        boolean allEmpty = (postalCode == null || postalCode.trim().isEmpty()) &&
                (dateFilter == null || "all".equals(dateFilter)) &&
                (startDate == null || startDate.trim().isEmpty()) &&
                (endDate == null || endDate.trim().isEmpty());

        if (allEmpty) {
            return courseService.listAllCourses();
        }

        // Utiliser le service pour rechercher avec tous les paramètres
        return courseService.findCoursesByPostalCodeAndDate(
                postalCode, dateFilter, postalRadius, startDate, endDate);
    }

    @Override
    public Map<String, Object> getContextParameters(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("searchPostalCode", request.getParameter("postalCode"));
        params.put("searchDateFilter", request.getParameter("dateFilter"));
        params.put("searchPostalRadius", request.getParameter("postalRadius"));
        params.put("searchStartDate", request.getParameter("startDate"));
        params.put("searchEndDate", request.getParameter("endDate"));
        return params;
    }
}