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
        System.out.println("=== GenericSearchStrategyImpl - DEBUT RECHERCHE ===");

        // Récupérer tous les paramètres du formulaire
        String postalCode = request.getParameter("postalCode");
        String dateFilter = request.getParameter("dateFilter");
        String postalRadiusStr = request.getParameter("postalRadius");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        // Debug : afficher les valeurs EXACTES reçues
        System.out.println("Paramètres reçus:");
        System.out.println("  - postalCode: '" + postalCode + "'");
        System.out.println("  - dateFilter: '" + dateFilter + "'");
        System.out.println("  - postalRadiusStr: '" + postalRadiusStr + "'");
        System.out.println("  - startDate: '" + startDate + "'");
        System.out.println("  - endDate: '" + endDate + "'");

        // Traiter le rayon
        int postalRadius = 0;
        if (postalRadiusStr != null && !postalRadiusStr.isEmpty() && !postalRadiusStr.equals("null")) {
            try {
                postalRadius = Integer.parseInt(postalRadiusStr);
                System.out.println("  - postalRadius: " + postalRadius);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de conversion du rayon: " + e.getMessage());
            }
        }

        // Vérifier si les paramètres sont réellement vides ou juste "null" en string
        boolean postalCodeEmpty = (postalCode == null || postalCode.trim().isEmpty() || postalCode.equals("null"));
        boolean dateFilterEmpty = (dateFilter == null || "all".equals(dateFilter) || dateFilter.equals("null"));
        boolean startDateEmpty = (startDate == null || startDate.trim().isEmpty() || startDate.equals("null"));
        boolean endDateEmpty = (endDate == null || endDate.trim().isEmpty() || endDate.equals("null"));

        System.out.println("Analyse des paramètres:");
        System.out.println("  - postalCodeEmpty: " + postalCodeEmpty);
        System.out.println("  - dateFilterEmpty: " + dateFilterEmpty);
        System.out.println("  - startDateEmpty: " + startDateEmpty);
        System.out.println("  - endDateEmpty: " + endDateEmpty);
        System.out.println("  - postalRadius: " + postalRadius);

        // Si tous les paramètres sont vides, retourner toutes les courses
        boolean allEmpty = postalCodeEmpty && dateFilterEmpty && startDateEmpty && endDateEmpty;

        System.out.println("Tous les paramètres sont vides? " + allEmpty);

        if (allEmpty) {
            System.out.println("Retour de toutes les courses");
            List<Course> allCourses = courseService.listAllCourses();
            System.out.println("Nombre total de courses: " + allCourses.size());
            return allCourses;
        }

        // Nettoyer les paramètres (remplacer "null" par null)
        String cleanPostalCode = postalCodeEmpty ? null : postalCode;
        String cleanDateFilter = dateFilterEmpty ? null : dateFilter;
        String cleanStartDate = startDateEmpty ? null : startDate;
        String cleanEndDate = endDateEmpty ? null : endDate;

        System.out.println("Paramètres nettoyés:");
        System.out.println("  - cleanPostalCode: '" + cleanPostalCode + "'");
        System.out.println("  - cleanDateFilter: '" + cleanDateFilter + "'");
        System.out.println("  - cleanStartDate: '" + cleanStartDate + "'");
        System.out.println("  - cleanEndDate: '" + cleanEndDate + "'");

        // Utiliser le service pour rechercher avec tous les paramètres
        List<Course> results = courseService.findCoursesByPostalCodeAndDate(
                cleanPostalCode, cleanDateFilter, postalRadius, cleanStartDate, cleanEndDate);

        System.out.println("Nombre de résultats trouvés: " + results.size());
        results.forEach(course -> System.out.println("  - " + course.getName() + " (" + course.getCity() + ", " + course.getZipCode() + ")"));

        System.out.println("=== GenericSearchStrategyImpl - FIN RECHERCHE ===");

        return results;
    }

    @Override
    public Map<String, Object> getContextParameters(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();

        String postalCode = request.getParameter("postalCode");
        String dateFilter = request.getParameter("dateFilter");
        String postalRadius = request.getParameter("postalRadius");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        // Nettoyer les paramètres pour éviter les "null" en string
        params.put("searchPostalCode", (postalCode != null && !postalCode.equals("null")) ? postalCode : null);
        params.put("searchDateFilter", (dateFilter != null && !dateFilter.equals("null")) ? dateFilter : null);
        params.put("searchPostalRadius", (postalRadius != null && !postalRadius.equals("null")) ? postalRadius : null);
        params.put("searchStartDate", (startDate != null && !startDate.equals("null")) ? startDate : null);
        params.put("searchEndDate", (endDate != null && !endDate.equals("null")) ? endDate : null);

        return params;
    }
}