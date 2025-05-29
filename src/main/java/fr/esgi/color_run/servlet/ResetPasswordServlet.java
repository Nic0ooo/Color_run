package fr.esgi.color_run.servlet;

import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.impl.MemberServiceImpl;
import fr.esgi.color_run.utils.VerificationCodeStorage;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private MemberService memberService;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (memberService == null) {
            memberService = new MemberServiceImpl(); // ✅ instanciation directe pour éviter le null
        }

        String method = req.getMethod();
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        if ("GET".equalsIgnoreCase(method)) {
            String token = req.getParameter("token");
            String email = VerificationCodeStorage.getEmailByToken(token);

            if (email != null) {
                context.setVariable("token", token);
                context.setVariable("pageTitle", "Réinitialisation du mot de passe");
                context.setVariable("page", "reset_password");
                engine.process("reset_password", context, resp.getWriter());
            } else {
                resp.sendRedirect("login?invalidToken=true");
            }

        } else if ("POST".equalsIgnoreCase(method)) {
            String token = req.getParameter("token");
            String password = req.getParameter("password");
            String email = VerificationCodeStorage.getEmailByToken(token);

            if (email != null && password != null && !password.trim().isEmpty()) {
                memberService.updatePasswordByEmail(email, password);
                VerificationCodeStorage.removeToken(token);
                resp.sendRedirect("login?passwordReset=true");
            } else {
                resp.sendRedirect("reset-password?token=" + token + "&error=true");
            }
        }
    }
}
