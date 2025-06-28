package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.impl.MemberServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/admin-users")
public class AdminUsersServlet extends HttpServlet {

    private final MemberService memberService = new MemberServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member current = (Member) req.getSession().getAttribute("member");

        if (current == null || current.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Member> allMembers = memberService.listAllMembers();

        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));
        context.setVariable("member", current);
        context.setVariable("users", allMembers);
        context.setVariable("roles", Arrays.asList(Role.values()));

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        engine.process("admin/users", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member current = (Member) req.getSession().getAttribute("member");

        if (current == null || current.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");

        if (idParam != null && !idParam.isEmpty()) {
            try {
                Long memberId = Long.parseLong(idParam);

                switch (action) {
                    case "promote":
                        String role = req.getParameter("role");
                        if (role != null && !role.isEmpty()) {
                            memberService.updateRole(memberId, role);
                        }
                        break;
                    case "delete":
                        // Empêcher la suppression de soi-même
                        if (!memberId.equals(current.getId())) {
                            memberService.deleteMember(memberId);
                        }
                        break;
                    default:
                        System.out.println("⚠️ Action inconnue : " + action);
                        break;
                }

            } catch (NumberFormatException e) {
                System.err.println("❌ ID invalide : " + idParam);
            }
        }

        // Redirection vers la même page après action
        resp.sendRedirect(req.getContextPath() + "/admin-users");
    }
}
