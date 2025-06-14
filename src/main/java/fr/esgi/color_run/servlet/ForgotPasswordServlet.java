package fr.esgi.color_run.servlet;

import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.EmailService;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.impl.MemberServiceImpl;
import fr.esgi.color_run.utils.VerificationCodeStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.UUID;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private MemberService memberService;
    private EmailService emailService;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (memberService == null) {
            memberService = new MemberServiceImpl();
        }
        if (emailService == null) {
            emailService = new EmailService();
        }

        String method = req.getMethod();
        System.out.println(">>> ForgotPasswordServlet called with method: " + method);

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
        );

        if ("GET".equalsIgnoreCase(method)) {
            context.setVariable("pageTitle", "Mot de passe oublié");
            context.setVariable("page", "forgot_password");
            engine.process("forgot_password", context, resp.getWriter());

        } else if ("POST".equalsIgnoreCase(method)) {
            String email = req.getParameter("email");

            if (email != null && memberService.existsByEmail(email)) {
                String token = UUID.randomUUID().toString();
                VerificationCodeStorage.storeToken(email, token, 15);

                String resetLink = req.getRequestURL().toString().replace("forgot-password", "reset-password")
                        + "?token=" + token;

                emailService.sendEmail(email, "Réinitialisation du mot de passe",
                        "Cliquez ici pour réinitialiser : " + resetLink);
            }

            context.setVariable("message", "Si un compte existe, un mail a été envoyé.");
            context.setVariable("pageTitle", "Mot de passe oublié");
            context.setVariable("page", "forgot_password");
            engine.process("forgot_password", context, resp.getWriter());
        }
    }
}
