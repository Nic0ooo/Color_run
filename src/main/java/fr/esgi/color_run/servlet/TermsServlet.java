package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;

@WebServlet(urlPatterns = {"/terms"})
public class TermsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("TermsServlet: doGet() called");

        // Récupérer la session et le membre connecté (si il y en a un)
        HttpSession session = req.getSession(false);
        Member member = (Member) (session != null ? session.getAttribute("member") : null);

        // Configuration du moteur de template Thymeleaf
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        // Ajouter les variables au contexte
        context.setVariable("member", member);
        context.setVariable("pageTitle", "Conditions d'utilisation et Règlements");

        // Déterminer quelle page afficher selon l'URL
        String requestURI = req.getRequestURI();
        String templateName;

        if (requestURI.endsWith("/rules")) {
            templateName = "rules";
            context.setVariable("pageTitle", "Règlement");
        } else {
            templateName = "terms";
            context.setVariable("pageTitle", "Conditions d'utilisation");
        }

        // Configuration de la réponse
        resp.setContentType("text/html;charset=UTF-8");

        // Rendu de la page
        engine.process(templateName, context, resp.getWriter());

        System.out.println("TermsServlet: Page " + templateName + " rendue avec succès");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Pour l'instant, les pages terms et rules sont en lecture seule
        // Si vous avez besoin de traiter des formulaires plus tard, vous pouvez les ajouter ici
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST non supporté pour cette page");
    }
}