package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;

@WebServlet("/")
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
        );

        // Récupérer le membre de la session
        Member member = (Member) req.getSession().getAttribute("member");
        context.setVariable("member", member);

        context.setVariable("pageTitle", "Accueil");

        engine.process("home", context, resp.getWriter());
    }
}
