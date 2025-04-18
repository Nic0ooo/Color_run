package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet(urlPatterns = {"/courses"})
public class CoursesServlet extends HttpServlet {

    private final CourseService courseService = new CourseServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("CoursesServlet: doGet() called");

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        // Récupérer la liste des courses passés
        var courses = courseService.listAllCourses();
        System.out.println("CoursesServlet: Nombre de courses récupérées = " + courses.size());
        context.setVariable("courses", courses);

        // Récupérer la liste des courses à venir
        var upcomingCourses = courseService.listUpcomingCourses();
        System.out.println("CoursesServlet: Nombre de courses à venir récupérées = " + upcomingCourses.size());
        context.setVariable("upcomingCourses", upcomingCourses);

        // Récupérer la liste des courses passés
        var pastCourses = courseService.listPastCourses();
        System.out.println("CoursesServlet: Nombre de courses récupérées = " + pastCourses.size());
        context.setVariable("pastCourses", pastCourses);

        // Récupérer le membre de la session
        Member member = (Member) req.getSession().getAttribute("member");
        context.setVariable("member", member);
        System.out.println("Connécté en tant que :" + member);

        // Configuration de la réponse
        resp.setContentType("text/html;charset=UTF-8");
        engine.process("courses", context, resp.getWriter());
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
}


