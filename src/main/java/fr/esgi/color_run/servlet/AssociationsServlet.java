package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
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
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet(urlPatterns = {"/associations", "/association-detail"})
public class AssociationsServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AssociationService associationService;
    private Association_memberService associationMemberService;
    private CourseService courseService;

    @Override
    public void init() throws ServletException {
        super.init();
        GeocodingService geocodingService = new GeocodingServiceImpl();
        CourseRepository courseRepository = new CourseRepositoryImpl(new GeocodingServiceImpl());
        this.associationService = new AssociationServiceImpl();
        this.associationMemberService = new Association_memberServiceImpl();
        this.courseService = new CourseServiceImpl(courseRepository, geocodingService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("AssociationsServlet: doGet() called");

        HttpSession session = req.getSession(false);
        Member member = (Member) (session != null ? session.getAttribute("member") : null);

        // Vérifier si c'est une requête AJAX
        String isAjax = req.getParameter("ajax");
        if ("true".equals(isAjax)) {
            handleAjaxRequest(req, resp);
            return;
        }

        // Traitement normal pour le rendu HTML
        handleNormalRequest(req, resp, member);
    }

    private void handleAjaxRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Récupération des paramètres de recherche et tri
        String searchTerm = req.getParameter("search");
        String sortBy = req.getParameter("sortBy");
        String sortDirection = req.getParameter("sortDirection");
        String associationFilter = req.getParameter("associationFilter"); // Nouveau paramètre

        // Récupération des paramètres de pagination
        int page = 1;
        int pageSize = 10;

        try {
            page = Integer.parseInt(req.getParameter("page"));
            if (req.getParameter("pageSize") != null) {
                pageSize = Integer.parseInt(req.getParameter("pageSize"));
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur de conversion des paramètres de pagination: " + e.getMessage());
        }

        // Récupérer le membre connecté pour les filtres
        HttpSession session = req.getSession(false);
        Member member = (Member) (session != null ? session.getAttribute("member") : null);

        try {
            // Récupérer les associations selon le filtre
            List<Association> allAssociations;

            if (member != null && "my-associations".equals(associationFilter)) {
                // Filtrer par associations du membre
                allAssociations = associationMemberService.getAssociationsByOrganizer(member.getId());
            } else {
                // Toutes les associations (comportement par défaut)
                allAssociations = associationService.getAllAssociations();
            }

            // Enrichir avec le nombre de courses et filtrer/trier
            List<AssociationWithCourseCount> enrichedAssociations = allAssociations.stream()
                    .map(this::enrichAssociationWithCourseCount)
                    .filter(assoc -> filterAssociation(assoc, searchTerm))
                    .sorted(getComparator(sortBy, sortDirection))
                    .collect(Collectors.toList());

            // Appliquer la pagination
            List<AssociationWithCourseCount> paginatedAssociations = paginateAssociations(enrichedAssociations, page, pageSize);

            // Créer les infos de pagination
            Map<String, Object> pagination = createPaginationInfo(page, pageSize, enrichedAssociations.size());

            // Créer la réponse JSON
            Map<String, Object> response = new HashMap<>();
            response.put("associations", convertAssociationsToJson(paginatedAssociations, member));
            response.put("pagination", pagination);

            // Configurer la réponse
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche AJAX: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleNormalRequest(HttpServletRequest req, HttpServletResponse resp, Member member) throws ServletException, IOException {
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        // Vérifier si appel association détail
        String associationId = req.getParameter("id");

        if (associationId != null && !associationId.isEmpty()) {
            showAssociationDetail(associationId, context, engine, resp, req, member);
        } else {
            try {
                // Récupérer la liste des associations avec le nombre de courses
                List<Association> associations = associationService.getAllAssociations();
                List<AssociationWithCourseCount> enrichedAssociations = associations.stream()
                        .map(this::enrichAssociationWithCourseCount)
                        .collect(Collectors.toList());

                context.setVariable("member", member);
                context.setVariable("associations", enrichedAssociations);
                context.setVariable("pageTitle", "Associations");

                if (member != null) {
                    context.setVariable("currentMemberId", member.getId());
                    // Récupérer les associations du membre pour les permissions
                    List<Association> memberAssociations = associationMemberService.getAssociationsByOrganizer(member.getId());
                    context.setVariable("memberAssociations", memberAssociations);

                    // Créer une map des IDs d'associations du membre pour faciliter les vérifications
                    Set<Long> memberAssociationIds = memberAssociations.stream()
                            .map(Association::getId)
                            .collect(Collectors.toSet());
                    context.setVariable("memberAssociationIds", memberAssociationIds);
                }

                // Configuration de la réponse
                resp.setContentType("text/html;charset=UTF-8");
                engine.process("associations", context, resp.getWriter());

            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des associations: " + e.getMessage());
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void showAssociationDetail(String associationId, WebContext context, TemplateEngine engine,
                                       HttpServletResponse resp, HttpServletRequest req, Member member) throws IOException {
        try {
            Optional<Association> associationOpt = associationService.getAssociationById(Long.parseLong(associationId));

            if (associationOpt.isEmpty()) {
                System.err.println("Association non trouvée avec l'ID: " + associationId);
                resp.sendRedirect(req.getContextPath() + "/associations");
                return;
            }

            Association association = associationOpt.get();

            // Récupérer les courses de cette association
            List<Course> associationCourses = courseService.getCoursesByAssociationId(Long.parseLong(associationId));

            // Séparer en courses à venir et passées
            List<Course> upcomingCourses = courseService.filterUpcomingCourses(associationCourses);
            List<Course> pastCourses = courseService.filterPastCourses(associationCourses);

            // Récupérer les organisateurs de cette association
            List<Member> organizers = associationMemberService.getOrganizersByAssociation(Long.parseLong(associationId));

            context.setVariable("association", association);
            context.setVariable("member", member);
            context.setVariable("upcomingCourses", upcomingCourses);
            context.setVariable("pastCourses", pastCourses);
            context.setVariable("organizers", organizers);
            context.setVariable("pageTitle", "Détail de l'association - " + association.getName());
            context.setVariable("contextPath", req.getContextPath());

            if (member != null) {
                context.setVariable("currentMemberId", member.getId());
                // Vérifier si le membre fait partie de cette association
                boolean isMemberOfAssociation = associationMemberService.isOrganizerInAssociation(member.getId(), Long.parseLong(associationId));
                context.setVariable("isMemberOfAssociation", isMemberOfAssociation);
            }

            resp.setContentType("text/html;charset=UTF-8");
            engine.process("association_detail", context, resp.getWriter());

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'association: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/associations");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("update".equals(action)) {
            handleUpdate(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action non reconnue.");
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long associationId = Long.parseLong(req.getParameter("associationId"));
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String email = req.getParameter("email");
            String phoneNumber = req.getParameter("phoneNumber");
            String address = req.getParameter("address");
            String city = req.getParameter("city");
            String websiteLink = req.getParameter("websiteLink");

            Integer zipCode = null;
            String zipCodeParam = req.getParameter("zipCode");
            if (zipCodeParam != null && !zipCodeParam.trim().isEmpty()) {
                try {
                    zipCode = Integer.parseInt(zipCodeParam.trim());
                } catch (NumberFormatException e) {
                    System.err.println("Code postal invalide: " + zipCodeParam);
                }
            }

            // Récupérer l'association existante
            Optional<Association> existingAssocOpt = associationService.getAssociationById(associationId);
            if (existingAssocOpt.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Association non trouvée.");
                return;
            }

            Association association = existingAssocOpt.get();
            association.setName(name);
            association.setDescription(description);
            association.setEmail(email);
            association.setPhoneNumber(phoneNumber);
            association.setAddress(address);
            association.setCity(city);
            association.setZipCode(zipCode);
            association.setWebsiteLink(websiteLink);

            associationService.updateAssociation(association);
            resp.sendRedirect(req.getContextPath() + "/associations?success=association_updated");

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'association: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/associations?error=update_failed");
        }
    }

    // Méthodes utilitaires

    private boolean canMemberEditAssociation(Member member, Association association) {
        if (member == null) return false;

        if (member.getRole() == Role.ADMIN) {
            return true;
        }

        if (member.getRole() == Role.ORGANIZER) {
            try {
                return associationMemberService.isOrganizerInAssociation(member.getId(), association.getId());
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification des permissions pour l'association " + association.getId());
                return false;
            }
        }

        return false;
    }

    private AssociationWithCourseCount enrichAssociationWithCourseCount(Association association) {
        try {
            List<Course> courses = courseService.getCoursesByAssociationId(association.getId());
            return new AssociationWithCourseCount(association, courses.size());
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des courses pour l'association " + association.getId());
            return new AssociationWithCourseCount(association, 0);
        }
    }

    private boolean filterAssociation(AssociationWithCourseCount association, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }

        String search = searchTerm.toLowerCase();
        Association assoc = association.getAssociation();

        return (assoc.getName() != null && assoc.getName().toLowerCase().contains(search)) ||
                (assoc.getCity() != null && assoc.getCity().toLowerCase().contains(search)) ||
                (assoc.getEmail() != null && assoc.getEmail().toLowerCase().contains(search)) ||
                (assoc.getZipCode() != null && assoc.getZipCode().toString().contains(search));
    }

    private Comparator<AssociationWithCourseCount> getComparator(String sortBy, String sortDirection) {
        Comparator<AssociationWithCourseCount> comparator = null;

        if (sortBy != null) {
            switch (sortBy) {
                case "name":
                    comparator = Comparator.comparing(a -> a.getAssociation().getName(), String.CASE_INSENSITIVE_ORDER);
                    break;
                case "city":
                    comparator = Comparator.comparing(a -> a.getAssociation().getCity(),
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                    break;
                case "email":
                    comparator = Comparator.comparing(a -> a.getAssociation().getEmail(),
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                    break;
                case "courseCount":
                    comparator = Comparator.comparing(AssociationWithCourseCount::getCourseCount);
                    break;
                default:
                    comparator = Comparator.comparing(a -> a.getAssociation().getName(), String.CASE_INSENSITIVE_ORDER);
                    break;
            }
        }

        if (comparator != null && "desc".equals(sortDirection)) {
            comparator = comparator.reversed();
        }

        return comparator != null ? comparator : Comparator.comparing(a -> a.getAssociation().getName(), String.CASE_INSENSITIVE_ORDER);
    }

    private List<AssociationWithCourseCount> paginateAssociations(List<AssociationWithCourseCount> associations, int page, int pageSize) {
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, associations.size());

        if (startIndex >= associations.size()) {
            return new ArrayList<>();
        }

        return associations.subList(startIndex, endIndex);
    }

    private Map<String, Object> createPaginationInfo(int currentPage, int pageSize, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", currentPage);
        pagination.put("pageSize", pageSize);
        pagination.put("totalPages", totalPages);
        pagination.put("totalItems", totalItems);

        return pagination;
    }

    private List<Map<String, Object>> convertAssociationsToJson(List<AssociationWithCourseCount> associations, Member member) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (AssociationWithCourseCount assocWithCount : associations) {
            Association association = assocWithCount.getAssociation();
            Map<String, Object> associationMap = new HashMap<>();

            associationMap.put("id", association.getId());
            associationMap.put("name", association.getName());
            associationMap.put("description", association.getDescription());
            associationMap.put("email", association.getEmail());
            associationMap.put("phoneNumber", association.getPhoneNumber());
            associationMap.put("address", association.getAddress());
            associationMap.put("city", association.getCity());
            associationMap.put("zipCode", association.getZipCode());
            associationMap.put("websiteLink", association.getWebsiteLink());
            associationMap.put("courseCount", assocWithCount.getCourseCount());

            // Déterminer les permissions
            boolean canEdit = canMemberEditAssociation(member, association);

            associationMap.put("canEdit", canEdit);

            result.add(associationMap);
        }

        return result;
    }

    // Classe interne pour associer une association avec son nombre de courses
    public static class AssociationWithCourseCount {
        private final Association association;
        private final int courseCount;

        public AssociationWithCourseCount(Association association, int courseCount) {
            this.association = association;
            this.courseCount = courseCount;
        }

        public Association getAssociation() {
            return association;
        }

        public int getCourseCount() {
            return courseCount;
        }
    }
}