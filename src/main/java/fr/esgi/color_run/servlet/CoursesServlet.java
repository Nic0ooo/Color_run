package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.Course_memberService;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import fr.esgi.color_run.service.impl.Course_memberServiceImpl;
import fr.esgi.color_run.service.impl.GeocodingServiceImpl;
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

import com.fasterxml.jackson.databind.ObjectMapper;


@WebServlet(urlPatterns = {"/courses", "/course-detail"})
public class CoursesServlet extends HttpServlet {

    //private final CourseService courseService = new CourseServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final CourseService courseService = new CourseServiceImpl();
    private CourseService courseService;
    private CourseRepository courseRepository;
    private Course_memberService courseMemberService;

    @Override
    public void init() throws ServletException {
        super.init();
        GeocodingService geocodingService = new GeocodingServiceImpl();
        CourseRepository courseRepository = new CourseRepositoryImpl(new GeocodingServiceImpl());
        this.courseService = new CourseServiceImpl(courseRepository, geocodingService);

        this.courseMemberService = new Course_memberServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("CoursesServlet: doGet() called");

        HttpSession session = req.getSession(false);
//        if (session == null || session.getAttribute("member") == null) {
//            resp.sendRedirect("login");
//            return;
//        }

//        Member member = (Member) session.getAttribute("member");
        Member member = (Member) (session != null ? session.getAttribute("member") : null);

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
        List<Course> upcomingCourses = courseService.searchAndSortCourses(
                searchTerm, fromDate, toDate, sortBy, sortDirection, true);
        List<Course> pastCourses = courseService.searchAndSortCourses(
                searchTerm, fromDate, toDate, sortBy, sortDirection, false);

        System.out.println("AJAX - Courses à venir: " + upcomingCourses.size());
        System.out.println("AJAX - Courses passées: " + pastCourses.size());

        // Créer la réponse JSON
        Map<String, Object> response = new HashMap<>();
        response.put("upcomingCourses", convertCoursesToJson(upcomingCourses));
        response.put("pastCourses", convertCoursesToJson(pastCourses));

        // Configurer la réponse
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(response));
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
//            List<Course> upcomingCourses = courseService.searchAndSortCourses(
//                    searchTerm, fromDate, toDate, sortBy, sortDirection, true);
//            List<Course> pastCourses = courseService.searchAndSortCourses(
//                    searchTerm, fromDate, toDate, sortBy, sortDirection, false);
            // Récupérer les courses avec filtres et tri
            List<Course> upcomingCourses = courseService.listUpcomingCourses();
            List<Course> pastCourses = courseService.listPastCourses();

            System.out.println("CoursesServlet: Nombre de courses à venir récupérées = " + upcomingCourses.size());
            System.out.println("CoursesServlet: Nombre de courses passées récupérées = " + pastCourses.size());

            context.setVariable("upcomingCourses", upcomingCourses);
            context.setVariable("pastCourses", pastCourses);

            // Configuration de la réponse
            resp.setContentType("text/html;charset=UTF-8");
            engine.process("courses", context, resp.getWriter());
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
            courseMap.put("currentNumberOfRunners", course.getCurrentNumberOfRunners());
            courseMap.put("associationId", course.getAssociationId());
            courseMap.put("memberCreatorId", course.getMemberCreatorId());

            result.add(courseMap);
        }

        return result;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("CoursesServlet: doPost() called");
        req.setCharacterEncoding("UTF-8");

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
            // Gestion des erreurs de parsing
        }

        double startLatitude = Double.parseDouble(req.getParameter("startLatitude"));
        double startLongitude = Double.parseDouble(req.getParameter("startLongitude"));
        double endLatitude = Double.parseDouble(req.getParameter("endLatitude"));
        double endLongitude = Double.parseDouble(req.getParameter("endLongitude"));
        double distance = Double.parseDouble(req.getParameter("distance"));
        int zipCode = Integer.parseInt(req.getParameter("zipCode"));
        int maxOfRunners = Integer.parseInt(req.getParameter("maxOfRunners"));
        int associationId = Integer.parseInt(req.getParameter("associationId"));
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

        courseService.createCourse(course);

        resp.sendRedirect(req.getContextPath() + "/courses");
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
            // Gestion des erreurs de parsing
        }

        double startLatitude = Double.parseDouble(req.getParameter("startLatitude"));
        double startLongitude = Double.parseDouble(req.getParameter("startLongitude"));
        double endLatitude = Double.parseDouble(req.getParameter("endLatitude"));
        double endLongitude = Double.parseDouble(req.getParameter("endLongitude"));
        double distance = Double.parseDouble(req.getParameter("distance"));
        int zipCode = Integer.parseInt(req.getParameter("zipCode"));
        int maxOfRunners = Integer.parseInt(req.getParameter("maxOfRunners"));
        int associationId = Integer.parseInt(req.getParameter("associationId"));
        int memberCreatorId = Integer.parseInt(req.getParameter("memberCreatorId"));
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
        course.setCurrentNumberOfRunners(0);
        course.setAssociationId(associationId);
        course.setMemberCreatorId(memberCreatorId);
        course.setPrice(price);

        courseService.updateCourse(course);

        resp.sendRedirect(req.getContextPath() + "/courses");
    }

    private void showCourseDetail(String courseId, WebContext context, TemplateEngine engine,
                                  HttpServletResponse resp, HttpServletRequest req, Member member) throws IOException {
        try {
            var course = courseService.getCourseById(Long.parseLong(courseId));

            if (course == null) {
                System.err.println("Course non trouvée avec l'ID: " + courseId);
                resp.sendRedirect(req.getContextPath() + "/courses");
                return;
            }

            // ✅ Vérifier si la course est expirée
            boolean courseExpired = false;
            if (course.getEndDate() != null) {
                courseExpired = course.getEndDate().isBefore(LocalDateTime.now());
            } else if (course.getStartDate() != null) {
                courseExpired = course.getStartDate().isBefore(LocalDateTime.now());
            }

            // ✅ Si tentative d'inscription sur course expirée via URL
            if (courseExpired && req.getParameter("error") == null) {
                resp.sendRedirect(req.getContextPath() + "/course-detail?id=" + courseId + "&error=course_expired");
                return;
            }

            // ✅ Vérifier l'état d'inscription via le SERVICE (pas le repository directement)
            boolean isUserRegistered = false;
            boolean isUserPaid = false;

            if (member != null) {
                Long courseIdLong = Long.parseLong(courseId);

                // ✅ Utilisation du service au lieu du repository
                isUserRegistered = courseMemberService.isMemberInCourse(courseIdLong, member.getId());
                isUserPaid = courseMemberService.isMemberRegisteredAndPaid(courseIdLong, member.getId());

                System.out.println("🔍 État inscription pour member " + member.getId() + " course " + courseId + ":");
                System.out.println("  - Inscrit: " + isUserRegistered);
                System.out.println("  - Payé: " + isUserPaid);
            }

            // ✅ Passer toutes les variables au contexte Thymeleaf
            context.setVariable("course", course);
            context.setVariable("member", member);
            context.setVariable("pageTitle", "Détail de la course - " + course.getName());
            context.setVariable("contextPath", req.getContextPath());
            context.setVariable("isUserRegistered", isUserRegistered);
            context.setVariable("isUserPaid", isUserPaid);

            System.out.println("🔍 Debug course detail:");
            System.out.println("  - Course: " + course.getName());
            System.out.println("  - Member: " + (member != null ? member.getEmail() : "null"));
            System.out.println("  - Prix: " + course.getPrice());
            System.out.println("  - User registered: " + isUserRegistered);
            System.out.println("  - User paid: " + isUserPaid);

            resp.setContentType("text/html;charset=UTF-8");
            engine.process("course_detail", context, resp.getWriter());

            System.out.println("✅ CoursesServlet: Détail affiché pour la course ID = " + courseId);

        } catch (NumberFormatException e) {
            System.err.println("❌ ID de course invalide: " + courseId);
            resp.sendRedirect(req.getContextPath() + "/courses");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de la course: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/courses");
        }
    }
}