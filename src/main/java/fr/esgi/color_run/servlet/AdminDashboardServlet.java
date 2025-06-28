package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.OrganizerRequestService;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import fr.esgi.color_run.service.impl.GeocodingServiceImpl;
import fr.esgi.color_run.service.impl.MemberServiceImpl;
import fr.esgi.color_run.service.impl.OrganizerRequestServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;

@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final MemberService memberService = new MemberServiceImpl();
    GeocodingServiceImpl geoService = new GeocodingServiceImpl();
    private final CourseService courseService =
            new CourseServiceImpl(new CourseRepositoryImpl(geoService), geoService);
    private final OrganizerRequestService organizerRequestService = new OrganizerRequestServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member current = (Member) req.getSession().getAttribute("member");
        if (current == null || current.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String tab = req.getParameter("tab") != null ? req.getParameter("tab") : "organizer-requests";

        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));
        context.setVariable("page", "admin-dashboard");
        context.setVariable("tab", tab);
        context.setVariable("member", current);

        switch (tab) {
            case "users":
                context.setVariable("users", memberService.listAllMembers());
                break;
            case "courses":
                context.setVariable("allCourses", courseService.listAllCourses());
                break;
            case "organizer-requests":
            default:
                try {
                    context.setVariable("pendingRequests", organizerRequestService.getPendingRequests());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    context.setVariable("allRequests", organizerRequestService.getAllRequests());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
        }

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        engine.process("admin-dashboard", context, resp.getWriter());
    }
}
