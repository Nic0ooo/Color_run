package fr.esgi.color_run.servlet;

import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//@WebServlet("/index")
public class IndexServlet extends HttpServlet {
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        // Configuration de la réponse
//        response.setContentType("text/html;charset=UTF-8");
//
//        // Récupérer le moteur Thymeleaf et créer le contexte
//        TemplateEngine templateEngine = ThymeleafConfiguration.getTemplateEngine();
//        WebContext context = new WebContext(
//                ThymeleafConfiguration.getApplication().buildExchange(request, response));
//
//        // Ajouter des variables au contexte
//        context.setVariable("pageTitle", "Bienvenue sur Color Run");
//        context.setVariable("currentYear", java.time.Year.now().getValue());
//
//        // Traiter le template et envoyer la réponse
//        templateEngine.process("index", context, response.getWriter());
//    }
}