package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.AssociationService;
import fr.esgi.color_run.service.Association_memberService;
import fr.esgi.color_run.service.impl.AssociationServiceImpl;
import fr.esgi.color_run.service.impl.Association_memberServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin-associations")
public class AdminAssociationsServlet extends HttpServlet {

    private final AssociationService associationService = new AssociationServiceImpl();
    private final Association_memberService associationMemberService = new Association_memberServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member current = (Member) req.getSession().getAttribute("member");
        if (current == null || current.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            List<Association> associations = associationService.getAllAssociations();
            for (Association assoc : associations) {
                assoc.setMembers(associationMemberService.getOrganizersByAssociation(assoc.getId()));
            }

            WebContext context = new WebContext(
                    ThymeleafConfiguration.getApplication().buildExchange(req, resp)
            );
            context.setVariable("member", current);
            context.setVariable("associations", associations);

            // Transmettre un message d'erreur si présent
            Object errorMessage = req.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage.toString());
            }

            ThymeleafConfiguration.getTemplateEngine()
                    .process("admin/associations", context, resp.getWriter());

        } catch (Exception e) {
            throw new ServletException("Erreur lors du chargement des associations admin", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member current = (Member) req.getSession().getAttribute("member");
        if (current == null || current.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("delete".equalsIgnoreCase(action)) {
                handleDelete(req);
            } else if ("edit".equalsIgnoreCase(action)) {
                handleEdit(req);
            }
            resp.sendRedirect(req.getContextPath() + "/admin-associations");
        } catch (Exception e) {
            // On affiche l'erreur dans la page HTML
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }

    private void handleDelete(HttpServletRequest req) throws Exception {
        Long id = Long.parseLong(req.getParameter("id"));
        associationService.deleteAssociation(id); // lève une exception en cas d’échec
    }

    private void handleEdit(HttpServletRequest req) throws Exception {
        Long id = Long.parseLong(req.getParameter("id"));
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String city = req.getParameter("city");

        Association assoc = new Association();
        assoc.setId(id);
        assoc.setName(name);
        assoc.setEmail(email);
        assoc.setCity(city);

        associationService.updateAssociation(assoc);
    }
}
