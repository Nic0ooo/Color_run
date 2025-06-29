package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.Course_memberService;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import fr.esgi.color_run.service.impl.Course_memberServiceImpl;
import fr.esgi.color_run.service.impl.GeocodingServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/admin-courses")
public class AdminCoursesServlet extends HttpServlet {

    private CourseService courseService;
    private Course_memberService courseMemberService;

    @Override
    public void init() throws ServletException {
        GeocodingServiceImpl geocodingService = new GeocodingServiceImpl();
        this.courseService = new CourseServiceImpl(new CourseRepositoryImpl(geocodingService), geocodingService);
        this.courseMemberService = new Course_memberServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member current = (Member) req.getSession().getAttribute("member");

        if (current == null || current.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Course> upcomingCourses = courseService.listUpcomingCourses();
        List<Course> pastCourses = courseService.listPastCourses();

        updateCoursesWithRealCounts(upcomingCourses);
        updateCoursesWithRealCounts(pastCourses);

        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));
        context.setVariable("member", current);
        context.setVariable("upcomingCourses", upcomingCourses);
        context.setVariable("pastCourses", pastCourses);
        context.setVariable("pageTitle", "Gestion des courses");

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        resp.setContentType("text/html;charset=UTF-8");
        engine.process("admin/courses", context, resp.getWriter());
    }

    private void updateCoursesWithRealCounts(List<Course> courses) {
        for (Course course : courses) {
            if (course.getId() != null) {
                int realCount = courseMemberService.countRegisteredAndPaidMembers(course.getId());
                course.setCurrentNumberOfRunners(realCount);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (!"update".equals(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action non supportée.");
            return;
        }

        try {
            Long courseId = Long.parseLong(req.getParameter("courseId"));
            Course existing = courseService.getCourseById(courseId);
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/admin-courses?error=not_found");
                return;
            }

            Course course = new Course();
            course.setId(courseId);
            course.setName(req.getParameter("name"));
            course.setCity(req.getParameter("city"));
            course.setAddress(req.getParameter("address"));
            course.setDescription(req.getParameter("description"));
            course.setZipCode(Integer.parseInt(req.getParameter("zipCode")));
            course.setDistance(Double.parseDouble(req.getParameter("distance")));
            course.setPrice(Double.parseDouble(req.getParameter("price")));
            course.setMaxOfRunners(Integer.parseInt(req.getParameter("maxOfRunners")));

            course.setStartpositionLatitude(Double.parseDouble(req.getParameter("startLatitude")));
            course.setStartpositionLongitude(Double.parseDouble(req.getParameter("startLongitude")));
            course.setEndpositionLatitude(Double.parseDouble(req.getParameter("endLatitude")));
            course.setEndpositionLongitude(Double.parseDouble(req.getParameter("endLongitude")));

            course.setStartDate(LocalDateTime.parse(req.getParameter("startDate")));
            course.setEndDate(LocalDateTime.parse(req.getParameter("endDate")));

            // Ne pas modifier ces valeurs
            course.setMemberCreatorId(existing.getMemberCreatorId());
            course.setCurrentNumberOfRunners(existing.getCurrentNumberOfRunners());
            course.setAssociationId(existing.getAssociationId());

            courseService.updateCourse(course);
            resp.sendRedirect(req.getContextPath() + "/admin-courses?success=updated");

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin-courses?error=update_failed");
        }
    }
}
