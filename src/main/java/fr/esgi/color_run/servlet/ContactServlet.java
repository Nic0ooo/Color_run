package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@WebServlet("/contact")
public class ContactServlet extends HttpServlet {

    private final TemplateEngine templateEngine = ThymeleafConfiguration.getTemplateEngine();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(request, response));

        // Récupérer le membre de la session
        Member member = (Member) request.getSession().getAttribute("member");
        context.setVariable("member", member);

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter out = response.getWriter()) {

            templateEngine.process("contact", context, stringWriter);
            out.write(stringWriter.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Erreur lors de l'affichage de la page de contact.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String name = request.getParameter("name");
        String fromEmail = request.getParameter("email");
        String subject = request.getParameter("subject");
        String message = request.getParameter("message");

        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(request, response));

        try {
            String composed = "Nom: " + name + "\nEmail: " + fromEmail + "\n\n" + message;
            emailService.sendEmail("polo76989@gmail.com", subject, composed);
            context.setVariable("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            context.setVariable("error", true);
        }

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter out = response.getWriter()) {

            templateEngine.process("contact", context, stringWriter);
            out.write(stringWriter.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Erreur lors du rendu de la page après envoi du message.");
        }
    }
}
