package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import fr.esgi.color_run.service.impl.GeocodingServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin-courses")
public class AdminCoursesServlet extends HttpServlet {

    private final CourseService courseService =
            new CourseServiceImpl(
                    new CourseRepositoryImpl(new GeocodingServiceImpl()),
                    new GeocodingServiceImpl()
            );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member current = (Member) req.getSession().getAttribute("member");

        if (current == null || current.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Course> upcomingCourses = courseService.listUpcomingCourses();
        List<Course> pastCourses = courseService.listPastCourses();

        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));
        context.setVariable("member", current);
        context.setVariable("upcomingCourses", upcomingCourses);
        context.setVariable("pastCourses", pastCourses);

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        engine.process("admin/courses", context, resp.getWriter());
    }
}
