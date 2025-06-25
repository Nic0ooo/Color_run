package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.*;
import fr.esgi.color_run.service.impl.Association_memberServiceImpl;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import fr.esgi.color_run.service.impl.GeocodingServiceImpl;
import fr.esgi.color_run.service.impl.search.CourseSearchStrategyFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/")
public class HomeServlet extends HttpServlet {

    private TemplateEngine engine;
    private CourseService courseService;
    private Association_memberService association_memberService;
    private CourseSearchStrategyFactory searchStrategyFactory;

    @Override
    public void init() throws ServletException {
        engine = ThymeleafConfiguration.getTemplateEngine();

        GeocodingService geocodingService = new GeocodingServiceImpl();
        CourseRepository courseRepository = new CourseRepositoryImpl(geocodingService);
        courseService = new CourseServiceImpl(courseRepository, geocodingService);
        searchStrategyFactory = new CourseSearchStrategyFactory(courseService, geocodingService);
        association_memberService = new Association_memberServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
        );

        // Récupérer le membre de la session
        Member member = (Member) req.getSession().getAttribute("member");
        context.setVariable("member", member);

        // Si le membre est un organisateur, vérifier s'il a une association
        if (member != null && member.getRole() == Role.ORGANIZER) {
            // verifier si l'organisateur a une association
            Association association = null;
            try {
                List<Association> associations = association_memberService.getAssociationsByOrganizer(member.getId());
                if (!associations.isEmpty()) {
                    association = associations.get(0); // Prendre la première association
                    // Ajouter l'association à la session
                    req.getSession().setAttribute("associationMember", association);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            context.setVariable("organizerAssociation", association);
            context.setVariable("organizerHasAssociation", association != null);
        }

        // Récupérer seulement les courses à venir depuis le service
        List<Course> courses = courseService.listUpcomingCourses();
        context.setVariable("courses", courses);

        context.setVariable("RoleMember", member != null ? member.getRole() : null);

        context.setVariable("pageTitle", "Accueil");

        resp.setContentType("text/html;charset=UTF-8");
        engine.process("home", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("=== POST REQUEST RECEIVED ===");

        req.getParameterMap().forEach((key, values) -> {
            System.out.println("  " + key + " = " + java.util.Arrays.toString(values));
        });

        // Vérifier si c'est une requête AJAX - améliorer la détection
        String acceptHeader = req.getHeader("Accept");
        String xRequestedWith = req.getHeader("X-Requested-With");
        String contentType = req.getContentType();
        boolean isAjaxRequest = "XMLHttpRequest".equals(xRequestedWith) ||
                (acceptHeader != null && acceptHeader.contains("application/json"));

        // Récupérer les membres de la session
        Member member = (Member) req.getSession().getAttribute("member");

        // Sélectionner la stratégie de recherche appropriée
        CourseSearchStrategy searchStrategy = searchStrategyFactory.getStrategy(req);
        System.out.println("Selected strategy: " + searchStrategy.getClass().getSimpleName());

        // Récupérer les courses via la stratégie
        List<Course> courses = searchStrategy.search(req);
        System.out.println("Found " + courses.size() + " courses");

        // Afficher les noms des courses trouvées pour debug
        courses.forEach(course -> System.out.println("  - " + course.getName() + " (" + course.getCity() + ", " + course.getZipCode() + ")"));

        if (isAjaxRequest) {
            // Répondre en JSON pour les requêtes AJAX
            System.out.println("Traitement en mode AJAX (JSON)");
            handleAjaxRequest(req, resp, courses, searchStrategy);
        } else {
            // Répondre en HTML pour les requêtes normales
            System.out.println("Traitement en mode normal (HTML)");
            handleNormalRequest(req, resp, member, courses, searchStrategy);
        }

        System.out.println("=== END POST REQUEST ===");
    }

    private void handleAjaxRequest(HttpServletRequest req, HttpServletResponse resp,
                                   List<Course> courses, CourseSearchStrategy searchStrategy)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("{");

        // Ajouter les courses
        jsonResponse.append("\"courses\":[");
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            if (i > 0) jsonResponse.append(",");

            jsonResponse.append("{");
            jsonResponse.append("\"id\":").append(course.getId()).append(",");
            jsonResponse.append("\"name\":\"").append(escapeJson(course.getName())).append("\",");
            jsonResponse.append("\"description\":\"").append(escapeJson(course.getDescription())).append("\",");
            jsonResponse.append("\"city\":\"").append(escapeJson(course.getCity())).append("\",");
            jsonResponse.append("\"zipCode\":").append(course.getZipCode()).append(",");
            jsonResponse.append("\"startDate\":\"").append(escapeJson(String.valueOf(course.getStartDate()))).append("\",");
            jsonResponse.append("\"price\":").append(course.getPrice()).append(",");
            jsonResponse.append("\"startpositionLatitude\":").append(course.getStartpositionLatitude()).append(",");
            jsonResponse.append("\"startpositionLongitude\":").append(course.getStartpositionLongitude());
            jsonResponse.append("}");
        }
        jsonResponse.append("]");

        // Ajouter les paramètres de contexte
        Map<String, Object> contextParams = searchStrategy.getContextParameters(req);
        for (Map.Entry<String, Object> entry : contextParams.entrySet()) {
            jsonResponse.append(",");
            jsonResponse.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                jsonResponse.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number) {
                jsonResponse.append(value);
            } else if (value instanceof Boolean) {
                jsonResponse.append(value);
            } else {
                jsonResponse.append("null");
            }
        }

        jsonResponse.append("}");

        // Envoyer la réponse
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse.toString());
        out.flush();

        System.out.println("JSON response sent: " + jsonResponse.toString());
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void handleNormalRequest(HttpServletRequest req, HttpServletResponse resp,
                                     Member member, List<Course> courses,
                                     CourseSearchStrategy searchStrategy)
            throws ServletException, IOException {

        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
        );

        context.setVariable("member", member);
        context.setVariable("courses", courses);
        context.setVariable("RoleMember", member != null ? member.getRole() : null);

        // Ajouter les paramètres de contexte spécifiques à la stratégie
        Map<String, Object> contextParams = searchStrategy.getContextParameters(req);
        for (Map.Entry<String, Object> entry : contextParams.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }

        context.setVariable("pageTitle", "Résultats de la recherche");

        resp.setContentType("text/html;charset=UTF-8");
        engine.process("home", context, resp.getWriter());
    }
}