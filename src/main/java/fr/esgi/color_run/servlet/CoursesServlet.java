package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.repository.AssociationRepository;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.*;
import fr.esgi.color_run.service.impl.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;


@WebServlet(urlPatterns = {"/courses", "/course-detail"})
public class CoursesServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CourseService courseService;
    private CourseRepository courseRepository;
    private Course_memberService courseMemberService;
    private MemberService memberService;
    private AssociationService associationService;
    private  Association_memberService associationMemberService;

    @Override
    public void init() throws ServletException {
        super.init();
        GeocodingService geocodingService = new GeocodingServiceImpl();
        CourseRepository courseRepository = new CourseRepositoryImpl(new GeocodingServiceImpl());
        this.courseService = new CourseServiceImpl(courseRepository, geocodingService);

        this.courseMemberService = new Course_memberServiceImpl();
        this.memberService = new MemberServiceImpl();
        this.associationService = new AssociationServiceImpl();
        this.associationMemberService = new Association_memberServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("CoursesServlet: doGet() called");

        HttpSession session = req.getSession(false);

        Member member = (Member) (session != null ? session.getAttribute("member") : null);

        if (member != null) {
            List<Association> memberAssociations = null;
            try {
                memberAssociations = associationMemberService.getAssociationsByOrganizer(member.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            req.setAttribute("memberAssociations", memberAssociations);

/*
            context.setVariable("currentMemberId", member.getId());
*/
        }

        // Vérifier si c'est une requête AJAX
        String isAjax = req.getParameter("ajax");
        if ("true".equals(isAjax)) {
            handleAjaxRequest(req, resp, member);
            return;
        }

        // Traitement normal pour le rendu HTML
        handleNormalRequest(req, resp, member);
    }

    private void handleAjaxRequest(HttpServletRequest req, HttpServletResponse resp, Member member) throws IOException {
        // Récupération des paramètres de recherche et tri
        String searchTerm = req.getParameter("search");
        String fromDateStr = req.getParameter("fromDate");
        String toDateStr = req.getParameter("toDate");
        String sortBy = req.getParameter("sortBy");
        String sortDirection = req.getParameter("sortDirection");
        String courseFilter = req.getParameter("courseFilter");

        // Récupération des paramètres de pagination
        int upcomingPage = 1;
        int pastPage = 1;
        int pageSize = 6;

        try {
            if (req.getParameter("upcomingPage") != null) {
                upcomingPage = Integer.parseInt(req.getParameter("upcomingPage"));
            }
            if (req.getParameter("pastPage") != null) {
                pastPage = Integer.parseInt(req.getParameter("pastPage"));
            }
            if (req.getParameter("pageSize") != null) {
                pageSize = Integer.parseInt(req.getParameter("pageSize"));
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur de conversion des paramètres de pagination: " + e.getMessage());
        }

        // Conversion des dates
        LocalDate fromDate = null;
        LocalDate toDate = null;
        try {
            if (fromDateStr != null && !fromDateStr.isEmpty()) {
                fromDate = LocalDate.parse(fromDateStr);
            }
            if (toDateStr != null && !toDateStr.isEmpty()) {
                toDate = LocalDate.parse(toDateStr);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing des dates: " + e.getMessage());
        }

        // Récupérer les courses en fonction du filtre
        List<Course> allUpcomingCourses;
        List<Course> allPastCourses;

        if (member != null && "my-created".equals(courseFilter)) {
            // Filtrer par courses créées par l'utilisateur
            allUpcomingCourses = courseService.searchAndSortCoursesByCreator(
                    searchTerm, fromDate, toDate, sortBy, sortDirection, true, member.getId());
            allPastCourses = courseService.searchAndSortCoursesByCreator(
                    searchTerm, fromDate, toDate, sortBy, sortDirection, false, member.getId());
        } else if (member != null && "my-registered".equals(courseFilter)) {
            // Filtrer par courses où l'utilisateur est inscrit
            allUpcomingCourses = getRegisteredCourses(member.getId(), searchTerm, fromDate, toDate, sortBy, sortDirection, true);
            allPastCourses = getRegisteredCourses(member.getId(), searchTerm, fromDate, toDate, sortBy, sortDirection, false);
        } else {
            // Toutes les courses (comportement par défaut)
            allUpcomingCourses = courseService.searchAndSortCourses(
                    searchTerm, fromDate, toDate, sortBy, sortDirection, true);
            allPastCourses = courseService.searchAndSortCourses(
                    searchTerm, fromDate, toDate, sortBy, sortDirection, false);
        }

        // Appliquer la pagination
        List<Course> paginatedUpcomingCourses = paginateCourses(allUpcomingCourses, upcomingPage, pageSize);
        List<Course> paginatedPastCourses = paginateCourses(allPastCourses, pastPage, pageSize);

        // Mise à jour des compteurs sur les courses PAGINÉES
        updateCoursesWithRealCounts(paginatedUpcomingCourses);
        updateCoursesWithRealCounts(paginatedPastCourses);

        System.out.println("AJAX - Filter: " + courseFilter + ", Total courses à venir: " + allUpcomingCourses.size() + ", page: " + upcomingPage);
        System.out.println("AJAX - Total courses passées: " + allPastCourses.size() + ", page: " + pastPage);
        System.out.println("AJAX - Courses à venir paginées: " + paginatedUpcomingCourses.size());
        System.out.println("AJAX - Courses passées paginées: " + paginatedPastCourses.size());

        // Créer les infos de pagination
        Map<String, Object> upcomingPagination = createPaginationInfo(upcomingPage, pageSize, allUpcomingCourses.size());
        Map<String, Object> pastPagination = createPaginationInfo(pastPage, pageSize, allPastCourses.size());

        // Créer la réponse JSON
        Map<String, Object> response = new HashMap<>();
        response.put("upcomingCourses", convertCoursesToJson(paginatedUpcomingCourses));
        response.put("pastCourses", convertCoursesToJson(paginatedPastCourses));
        response.put("upcomingPagination", upcomingPagination);
        response.put("pastPagination", pastPagination);

        // Configurer la réponse
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(response));
    }

    /**
     * Récupère les courses où un membre est inscrit, avec filtres et tri
     */
    private List<Course> getRegisteredCourses(Long memberId, String searchTerm, LocalDate fromDate, LocalDate toDate,
                                              String sortBy, String sortDirection, boolean upcoming) {
        // Récupérer les courses où le membre est inscrit
        List<Course> memberCourses = upcoming
                ? courseMemberService.findUpcomingCoursesByMemberId(memberId)
                : courseMemberService.findPastCoursesByMemberId(memberId);

        // Appliquer les filtres manuellement
        List<Course> filteredCourses = memberCourses.stream()
                .filter(course -> {
                    // Filtre par terme de recherche
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        String search = searchTerm.toLowerCase();
                        if (!(course.getName().toLowerCase().contains(search) ||
                                course.getCity().toLowerCase().contains(search) ||
                                course.getZipCode().toString().contains(search))) {
                            return false;
                        }
                    }

                    // Filtre par date
                    if (fromDate != null && course.getStartDate() != null &&
                            course.getStartDate().toLocalDate().isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && course.getStartDate() != null &&
                            course.getStartDate().toLocalDate().isAfter(toDate)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Appliquer le tri
        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Course> comparator = getComparator(sortBy);
            if (comparator != null) {
                if ("desc".equals(sortDirection)) {
                    comparator = comparator.reversed();
                }
                filteredCourses.sort(comparator);
            }
        }

        return filteredCourses;
    }

    /**
     * Retourne un comparateur pour le tri des courses
     */
    private Comparator<Course> getComparator(String sortBy) {
        switch (sortBy) {
            case "name":
                return Comparator.comparing(Course::getName, String.CASE_INSENSITIVE_ORDER);
            case "startDate":
                return Comparator.comparing(Course::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "city":
                return Comparator.comparing(Course::getCity, String.CASE_INSENSITIVE_ORDER);
            case "distance":
                return Comparator.comparing(Course::getDistance);
            default:
                return null;
        }
    }

    /**
     * Pagine une liste de courses
     */
    private List<Course> paginateCourses(List<Course> courses, int page, int pageSize) {
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, courses.size());

        if (startIndex >= courses.size()) {
            return new ArrayList<>();
        }

        return courses.subList(startIndex, endIndex);
    }

    /**
     * Crée les informations de pagination
     */
    private Map<String, Object> createPaginationInfo(int currentPage, int pageSize, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", currentPage);
        pagination.put("pageSize", pageSize);
        pagination.put("totalPages", totalPages);
        pagination.put("totalItems", totalItems);

        return pagination;
    }

    private void handleNormalRequest(HttpServletRequest req, HttpServletResponse resp, Member member) throws ServletException, IOException {
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));


        // Vérifier si appel course détail
        String courseId = req.getParameter("id");

        if (courseId != null && !courseId.isEmpty()) {

            showCourseDetail(courseId, context, engine, resp, req, member);
        } else {

            // Récupérer la liste des courses
            var courses = courseService.listAllCourses();

            // IMPORTANT: Ajouter les informations du membre au contexte AVANT de les utiliser
            context.setVariable("member", member);

            if (member != null) {
                // Passer l'ID du membre au template pour les vérifications JavaScript
                context.setVariable("currentMemberId", member.getId());

                // Debug pour vérifier les valeurs
                System.out.println("🔍 Debug servlet - Member ID: " + member.getId());
                System.out.println("🔍 Debug servlet - Member role: " + member.getRole().name());
            } else {
                context.setVariable("currentMemberId", null);
                System.out.println("🔍 Debug servlet - No member connected");
            }

            System.out.println("CoursesServlet: Nombre de courses récupérées = " + courses.size());
            context.setVariable("courses", courses);
            context.setVariable("member", member);
            context.setVariable("pageTitle", "Courses");

            // Récupération des paramètres de recherche et tri
            String searchTerm = req.getParameter("search");
            String fromDateStr = req.getParameter("fromDate");
            String toDateStr = req.getParameter("toDate");
            String sortBy = req.getParameter("sortBy");
            String sortDirection = req.getParameter("sortDirection");

            // Conversion des dates
            LocalDate fromDate = null;
            LocalDate toDate = null;
            try {
                if (fromDateStr != null && !fromDateStr.isEmpty()) {
                    fromDate = LocalDate.parse(fromDateStr);
                }
                if (toDateStr != null && !toDateStr.isEmpty()) {
                    toDate = LocalDate.parse(toDateStr);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du parsing des dates: " + e.getMessage());
            }

            // Récupérer les courses avec filtres et tri
            List<Course> upcomingCourses = courseService.listUpcomingCourses();
            List<Course> pastCourses = courseService.listPastCourses();

            // ✅ MISE À JOUR DES COMPTEURS RÉELS
            updateCoursesWithRealCounts(upcomingCourses);
            updateCoursesWithRealCounts(pastCourses);

            System.out.println("CoursesServlet: Nombre de courses à venir récupérées = " + upcomingCourses.size());
            System.out.println("CoursesServlet: Nombre de courses passées récupérées = " + pastCourses.size());

            context.setVariable("upcomingCourses", upcomingCourses);
            context.setVariable("pastCourses", pastCourses);

            // Configuration de la réponse
            resp.setContentType("text/html;charset=UTF-8");
            engine.process("courses", context, resp.getWriter());
        }
    }

    // ✅ NOUVEAU : Mise à jour des courses avec les compteurs réels
    private void updateCoursesWithRealCounts(List<Course> courses) {
        for (Course course : courses) {
            if (course.getId() != null) {
                int realCount = courseMemberService.countRegisteredAndPaidMembers(course.getId());
                course.setCurrentNumberOfRunners(realCount);
                System.out.println("🔄 Course " + course.getName() + " (" + course.getId() + "): " + realCount + " participants payés");
            }
        }
    }

    private List<Map<String, Object>> convertCoursesToJson(List<Course> courses) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

        for (Course course : courses) {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", course.getId());
            courseMap.put("name", course.getName());
            courseMap.put("description", course.getDescription());
            courseMap.put("city", course.getCity());
            courseMap.put("zipCode", course.getZipCode());
            courseMap.put("address", course.getAddress());
            courseMap.put("distance", course.getDistance());
            courseMap.put("price", course.getPrice());
            courseMap.put("startDate", course.getStartDate() != null ? course.getStartDate().toString() : null);
            courseMap.put("endDate", course.getEndDate() != null ? course.getEndDate().toString() : null);
            courseMap.put("formattedStartDate", course.getStartDate() != null ? course.getStartDate().format(formatter) : "");
            courseMap.put("startpositionLatitude", course.getStartpositionLatitude());
            courseMap.put("startpositionLongitude", course.getStartpositionLongitude());
            courseMap.put("endpositionLatitude", course.getEndpositionLatitude());
            courseMap.put("endpositionLongitude", course.getEndpositionLongitude());
            courseMap.put("maxOfRunners", course.getMaxOfRunners());

            // ✅ UNE SEULE FOIS : currentNumberOfRunners (déjà mis à jour par updateCoursesWithRealCounts)
            courseMap.put("currentNumberOfRunners", course.getCurrentNumberOfRunners());

            // ✅ UNE SEULE FOIS : associationId avec gestion null
            courseMap.put("associationId", course.getAssociationId() != null ? course.getAssociationId() : 0);
            courseMap.put("memberCreatorId", course.getMemberCreatorId());

            // Calculs dérivés
            int placesRestantes = course.getMaxOfRunners() - course.getCurrentNumberOfRunners();
            courseMap.put("placesRestantes", placesRestantes);
            courseMap.put("isComplet", placesRestantes <= 0);
            courseMap.put("isPeuDePlace", placesRestantes > 0 && placesRestantes <= 10);
            courseMap.put("tauxRemplissage", course.getMaxOfRunners() > 0 ?
                    Math.round((double) course.getCurrentNumberOfRunners() / course.getMaxOfRunners() * 100) : 0);

            result.add(courseMap);
        }

        return result;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("CoursesServlet: doPost() called");
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        if ("create".equals(action)) {
            handleCreate(req, resp);
        } else if ("delete".equals(action)) {
            //Supression d'une course
            //handleDelete(req, resp);
        } else if ("update".equals(action)) {
            // Modif d'une course
            handleUpdate(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action non reconnue.");
        }
    }

    // MODIFICATION SIMILAIRE POUR handleCreate (pour la cohérence)
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String city = req.getParameter("city");
        String address = req.getParameter("address");

        // Convertir les dates
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        try {
            if (req.getParameter("startDate") != null && !req.getParameter("startDate").isEmpty()) {
                startDate = LocalDateTime.parse(req.getParameter("startDate").replace(" ", "T"));
            }
            if (req.getParameter("endDate") != null && !req.getParameter("endDate").isEmpty()) {
                endDate = LocalDateTime.parse(req.getParameter("endDate").replace(" ", "T"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // VALIDATION DES COORDONNÉES - NOUVEAU
        String startLatStr = req.getParameter("startLatitude");
        String startLongStr = req.getParameter("startLongitude");
        String endLatStr = req.getParameter("endLatitude");
        String endLongStr = req.getParameter("endLongitude");

        if (startLatStr == null || startLatStr.isEmpty() ||
                startLongStr == null || startLongStr.isEmpty() ||
                endLatStr == null || endLatStr.isEmpty() ||
                endLongStr == null || endLongStr.isEmpty()) {

            resp.sendRedirect(req.getContextPath() + "/courses?error=missing_coordinates");
            return;
        }

        double startLatitude, startLongitude, endLatitude, endLongitude;
        try {
            startLatitude = Double.parseDouble(startLatStr);
            startLongitude = Double.parseDouble(startLongStr);
            endLatitude = Double.parseDouble(endLatStr);
            endLongitude = Double.parseDouble(endLongStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/courses?error=invalid_coordinates");
            return;
        }

        double distance = Double.parseDouble(req.getParameter("distance"));
        int zipCode = Integer.parseInt(req.getParameter("zipCode"));
        int maxOfRunners = Integer.parseInt(req.getParameter("maxOfRunners"));

        // GESTION COHÉRENTE DE L'ASSOCIATION ID
        Integer associationId = null;
        String associationIdParam = req.getParameter("associationId");

        if (associationIdParam != null && !associationIdParam.trim().isEmpty()) {
            try {
                int tempAssocId = Integer.parseInt(associationIdParam.trim());
                if (tempAssocId > 0) {
                    associationId = tempAssocId;
                }
            } catch (NumberFormatException e) {
                System.err.println("Association ID invalide lors de la création, aucune association sera assignée.");
            }
        }

        int memberCreatorId = Integer.parseInt(req.getParameter("memberCreatorId"));
        double price = Double.parseDouble(req.getParameter("price"));

        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setCity(city);
        course.setAddress(address);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setStartpositionLatitude(startLatitude);
        course.setStartpositionLongitude(startLongitude);
        course.setEndpositionLatitude(endLatitude);
        course.setEndpositionLongitude(endLongitude);
        course.setDistance(distance);
        course.setZipCode(zipCode);
        course.setMaxOfRunners(maxOfRunners);
        course.setCurrentNumberOfRunners(0);
        course.setAssociationId(associationId);
        course.setMemberCreatorId(memberCreatorId);
        course.setPrice(price);

        try {
            courseService.createCourse(course);
            resp.sendRedirect(req.getContextPath() + "/courses?success=course_created");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la course: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/courses?error=creation_failed");
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long courseId = Long.parseLong(req.getParameter("courseId"));
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String city = req.getParameter("city");
        String address = req.getParameter("address");

        // Convertir les dates
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        try {
            if (req.getParameter("startDate") != null && !req.getParameter("startDate").isEmpty()) {
                startDate = LocalDateTime.parse(req.getParameter("startDate").replace(" ", "T"));
            }
            if (req.getParameter("endDate") != null && !req.getParameter("endDate").isEmpty()) {
                endDate = LocalDateTime.parse(req.getParameter("endDate").replace(" ", "T"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // VALIDATION DES COORDONNÉES - NOUVEAU
        String startLatStr = req.getParameter("startLatitude");
        String startLongStr = req.getParameter("startLongitude");
        String endLatStr = req.getParameter("endLatitude");
        String endLongStr = req.getParameter("endLongitude");

        if (startLatStr == null || startLatStr.isEmpty() ||
                startLongStr == null || startLongStr.isEmpty() ||
                endLatStr == null || endLatStr.isEmpty() ||
                endLongStr == null || endLongStr.isEmpty()) {

            // Rediriger avec un message d'erreur
            resp.sendRedirect(req.getContextPath() + "/courses?error=missing_coordinates");
            return;
        }

        double startLatitude, startLongitude, endLatitude, endLongitude;
        try {
            startLatitude = Double.parseDouble(startLatStr);
            startLongitude = Double.parseDouble(startLongStr);
            endLatitude = Double.parseDouble(endLatStr);
            endLongitude = Double.parseDouble(endLongStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/courses?error=invalid_coordinates");
            return;
        }

        double distance = Double.parseDouble(req.getParameter("distance"));
        int zipCode = Integer.parseInt(req.getParameter("zipCode"));
        int maxOfRunners = Integer.parseInt(req.getParameter("maxOfRunners"));

        // GESTION AMÉLIORÉE DE L'ASSOCIATION ID - MODIFIÉ
        Integer associationId = null;
        String associationIdParam = req.getParameter("associationId");

        if (associationIdParam != null && !associationIdParam.trim().isEmpty()) {
            try {
                int tempAssocId = Integer.parseInt(associationIdParam.trim());
                // Si la valeur est 0 ou négative, on considère qu'aucune association n'est sélectionnée
                if (tempAssocId > 0) {
                    associationId = tempAssocId;
                }
                // Sinon associationId reste null
            } catch (NumberFormatException e) {
                System.err.println("Association ID invalide: '" + associationIdParam + "', aucune association sera assignée.");
                // associationId reste null
            }
        }

        System.out.println("🔧 Association ID final: " + associationId);

        // IMPORTANT: Ne pas modifier le memberCreatorId lors d'une mise à jour
        // Récupérer la course existante pour conserver le créateur original
        Course existingCourse = courseService.getCourseById(courseId);
        if (existingCourse == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Course non trouvée.");
            return;
        }

        double price = Double.parseDouble(req.getParameter("price"));

        Course course = new Course();
        course.setId(courseId);
        course.setName(name);
        course.setDescription(description);
        course.setCity(city);
        course.setAddress(address);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setStartpositionLatitude(startLatitude);
        course.setStartpositionLongitude(startLongitude);
        course.setEndpositionLatitude(endLatitude);
        course.setEndpositionLongitude(endLongitude);
        course.setDistance(distance);
        course.setZipCode(zipCode);
        course.setMaxOfRunners(maxOfRunners);
        course.setCurrentNumberOfRunners(existingCourse.getCurrentNumberOfRunners()); // Conserver le nombre actuel

        // ASSIGNATION DE L'ASSOCIATION ID (peut être null) - MODIFIÉ
        course.setAssociationId(associationId);

        // CONSERVER le memberCreatorId original - ne jamais le modifier lors d'une mise à jour
        course.setMemberCreatorId(existingCourse.getMemberCreatorId());

        course.setPrice(price);

        try {
            courseService.updateCourse(course);
            resp.sendRedirect(req.getContextPath() + "/courses?success=course_updated");
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de la course: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/courses?error=update_failed");
        }
    }

    // ✅ MÉTHODE MISE À JOUR pour showCourseDetail avec calcul des participants

    private void showCourseDetail(String courseId, WebContext context, TemplateEngine engine,
                                  HttpServletResponse resp, HttpServletRequest req, Member member) throws IOException {
        System.out.println("🔍 DEBUG: showCourseDetail appelé avec courseId = " + courseId);

        try {
            System.out.println("🔍 DEBUG: Tentative getCourseById...");
            var course = courseService.getCourseById(Long.parseLong(courseId));

            if (course == null) {
                System.err.println("🔍 DEBUG: Course NULL - redirection vers /courses");
                resp.sendRedirect(req.getContextPath() + "/courses");
                return;
            }

            // Récupérer les informations de l'association
            Optional<Association> associationOpt = Optional.empty();
            if (course.getAssociationId() != null) {
                associationOpt = associationService.findById(Long.valueOf(course.getAssociationId()));
            }

            // Récupérer les informations du membre créateur
            Optional<Member> creatorOpt = Optional.empty();
            if (course.getMemberCreatorId() != null) {
                creatorOpt = memberService.getMember(Long.valueOf(course.getMemberCreatorId()));
            }

            context.setVariable("association", associationOpt.orElse(null));
            context.setVariable("creator", creatorOpt.orElse(null));

            System.out.println("🔍 DEBUG: Course trouvée = " + course.getName());

            // ✅ NOUVEAU : Calculer le nombre réel d'inscrits payés
            Long courseIdLong = Long.parseLong(courseId);
            int realRegisteredCount = courseMemberService.countRegisteredAndPaidMembers(courseIdLong);

            // ✅ Mettre à jour le nombre réel de participants dans l'objet course
            course.setCurrentNumberOfRunners(realRegisteredCount);

            // ✅ Calculer les places restantes
            int placesRestantes = course.getMaxOfRunners() - realRegisteredCount;

            System.out.println("📊 Statistiques course " + courseId + ":");
            System.out.println("   - Participants réels: " + realRegisteredCount);
            System.out.println("   - Places max: " + course.getMaxOfRunners());
            System.out.println("   - Places restantes: " + placesRestantes);

            // Vérifier si la course est expirée
            boolean courseExpired = false;
            if (course.getEndDate() != null) {
                courseExpired = course.getEndDate().isBefore(LocalDateTime.now());
            } else if (course.getStartDate() != null) {
                courseExpired = course.getStartDate().isBefore(LocalDateTime.now());
            }

            // Si tentative d'inscription sur course expirée via URL
            if (courseExpired && req.getParameter("error") == null) {
                resp.sendRedirect(req.getContextPath() + "/course-detail?id=" + courseId + "&error=course_expired");
                return;
            }

            // Vérifier l'état d'inscription via le SERVICE
            boolean isUserRegistered = false;
            boolean isUserPaid = false;
            String bibNumber = null;

            if (member != null) {
                Role memberRole = member.getRole();
                boolean isModerator = (memberRole == Role.ADMIN || memberRole == Role.ORGANIZER);

                if (isModerator) {
                    // Les modérateurs ont accès direct au chat
                    isUserRegistered = true;
                    isUserPaid = true;
                    System.out.println("✅ Accès modérateur accordé à " + member.getId() + " (" + memberRole + ") pour course " + courseId);
                } else {
                    // Pour les RUNNER : vérification classique
                    isUserRegistered = courseMemberService.isMemberInCourse(courseIdLong, member.getId());
                    isUserPaid = courseMemberService.isMemberRegisteredAndPaid(courseIdLong, member.getId());

                    if (isUserPaid) {
                        Optional<Course_member> registrationOpt = courseMemberService.getRegistrationDetails(courseIdLong, member.getId());
                        if (registrationOpt.isPresent()) {
                            bibNumber = registrationOpt.get().getBibNumber();
                            System.out.println("🎫 Dossard pour member " + member.getId() + " course " + courseId + ": " + bibNumber);
                        }
                    }
                }
                System.out.println("🔍 État inscription pour member " + member.getId() + " course " + courseId + ":");
                System.out.println("  - Inscrit: " + isUserRegistered);
                System.out.println("  - Payé: " + isUserPaid);
                System.out.println("  - Dossard: " + bibNumber);
            } else {
                System.out.println("🔍 DEBUG: Aucun member connecté");
            }

            System.out.println("🔍 DEBUG: Avant configuration context...");

            context.setVariable("course", course);
            context.setVariable("member", member);
            context.setVariable("pageTitle", "Détail de la course - " + course.getName());
            context.setVariable("contextPath", req.getContextPath());
            context.setVariable("isUserRegistered", isUserRegistered);
            context.setVariable("isUserPaid", isUserPaid);
            context.setVariable("bibNumber", bibNumber);

            // ✅ NOUVEAU : Ajouter les statistiques pour l'affichage
            context.setVariable("placesRestantes", placesRestantes);
            context.setVariable("realRegisteredCount", realRegisteredCount);

            System.out.println("🔍 DEBUG: Avant process template...");
            resp.setContentType("text/html;charset=UTF-8");
            engine.process("course_detail", context, resp.getWriter());

            System.out.println("🔍 DEBUG: Template processé avec succès !");
            System.out.println("CoursesServlet: Détail affiché pour la course ID = " + courseId);

        } catch (NumberFormatException e) {
            System.err.println("🔍 DEBUG: NumberFormatException - ID de course invalide: " + courseId);
            resp.sendRedirect(req.getContextPath() + "/courses");
        } catch (Exception e) {
            System.err.println("🔍 DEBUG: EXCEPTION dans showCourseDetail: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/courses");
        }
    }
}