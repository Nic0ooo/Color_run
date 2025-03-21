package fr.esgi.color_run.servlet;

import fr.esgi.color_run.configuration.ThymeleafConfiguration;
// Import commentés jusqu'à la mise en place du service
// import fr.esgi.color_run.service.EventService;
// import fr.esgi.color_run.service.impl.EventServiceImpl;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(urlPatterns = {"", "/", "/index"})
public class IndexServlet extends HttpServlet {

    // Service commenté pour le moment
    // private EventService eventService;

    @Override
    public void init() throws ServletException {
        // Initialisation commentée
        // this.eventService = new EventServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuration de la réponse
        response.setContentType("text/html;charset=UTF-8");

        // Fournir une liste vide au lieu des événements du service
        request.setAttribute("events", new ArrayList<>());

        // Variables pour le template base
        request.setAttribute("title", "Color Run - Course colorée et festive");
        request.setAttribute("content", "pages/index :: content");
        request.setAttribute("links", "pages/index :: links");
        request.setAttribute("scripts", "pages/index :: scripts");
        request.setAttribute("currentPage", "home");
        request.setAttribute("currentYear", java.time.Year.now().getValue());

        // Récupérer le moteur Thymeleaf et créer le contexte
        TemplateEngine templateEngine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(request, response));

        // Ajouter les attributs de la requête au contexte
        for (String attrName : java.util.Collections.list(request.getAttributeNames())) {
            context.setVariable(attrName, request.getAttribute(attrName));
        }

        // Traiter le template et envoyer la réponse
        templateEngine.process("base", context, response.getWriter());
    }
}