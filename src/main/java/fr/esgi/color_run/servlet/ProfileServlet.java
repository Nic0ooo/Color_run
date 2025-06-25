package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.impl.MemberServiceImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {

    private final MemberService memberService = new MemberServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Vérification de la session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("member") == null) {
            resp.sendRedirect("login?redirect=/profile");
            return;
        }
        Member member = (Member) session.getAttribute("member");

        // Récupère exactement le même TemplateEngine et WebContext que vos autres servlets
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration
                        .getApplication()                   // JakartaServletWebApplication
                        .buildExchange(req, resp)           // échange request/response
        );

        // Variables Thymeleaf
        context.setVariable("pageTitle", "Color Run | Mon Profil");
        context.setVariable("page", "profile");
        context.setVariable("member", member);

        resp.setContentType("text/html; charset=UTF-8");
        engine.process("profile", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("member") == null) {
            resp.sendRedirect("login?redirect=/profile");
            return;
        }
        Member member = (Member) session.getAttribute("member");

        // Mise à jour des champs
        member.setName(req.getParameter("name"));
        member.setFirstname(req.getParameter("firstname"));
        member.setPhoneNumber(req.getParameter("phoneNumber"));
        member.setAddress(req.getParameter("address"));
        member.setCity(req.getParameter("city"));
        String zip = req.getParameter("zipCode");
        if (zip != null && !zip.isEmpty()) {
            member.setZipCode(Integer.parseInt(zip));
        }

        Member updated = memberService.updateMember(member.getId(), member);
        session.setAttribute("member", updated);

        // Re-render
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
        );
        context.setVariable("pageTitle", "Color Run | Mon Profil");
        context.setVariable("page", "profile");
        context.setVariable("member", updated);
        context.setVariable("success", "Profil mis à jour avec succès !");

        resp.setContentType("text/html; charset=UTF-8");
        engine.process("profile", context, resp.getWriter());
    }
}
