package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
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
import java.util.Optional;

@WebServlet(urlPatterns = {"/login", "/register", "/verify"})
public class MemberServlet extends HttpServlet {

    private final MemberService memberService;

    public MemberServlet() {
        this.memberService = new MemberServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        switch (req.getServletPath()) {
            case "/login":
                context.setVariable("pageTitle", "Connexion");
                context.setVariable("page", "login");
                engine.process("login", context, resp.getWriter());
                break;

            case "/register":
                context.setVariable("pageTitle", "Inscription");
                context.setVariable("page", "register");
                engine.process("register", context, resp.getWriter());
                break;

            case "/verify":
                context.setVariable("pageTitle", "Vérification");
                context.setVariable("page", "verify");
                engine.process("verify", context, resp.getWriter());
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        switch (path) {
            case "/register":
                String name = req.getParameter("name");
                String firstname = req.getParameter("firstname");
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                String phone = req.getParameter("phoneNumber");
                String address = req.getParameter("address");
                String city = req.getParameter("city");
                String zip = req.getParameter("zipCode");

                Member newMember = new Member();
                newMember.setName(name);
                newMember.setFirstname(firstname);
                newMember.setEmail(email);
                newMember.setPassword(password);
                newMember.setPhoneNumber(phone != null ? phone : "");
                newMember.setAddress(address != null ? address : "");
                newMember.setCity(city != null ? city : "");
                newMember.setZipCode(zip != null && !zip.isEmpty() ? Integer.parseInt(zip) : 0);
                newMember.setRole(Role.valueOf("RUNNER"));
                newMember.setPositionLatitude(48.8566);
                newMember.setPositionLongitude(2.3522);

                if (memberService.findByEmail(email).isPresent()) {
                    context.setVariable("error", "Un compte avec cet email existe déjà.");
                    context.setVariable("page", "register");
                    engine.process("register", context, resp.getWriter());
                    return;
                }

                // Création du membre
                memberService.createMember(newMember);

                // Envoi du code
                String code = memberService.generateVerificationCodeForEmail(email);
                new EmailService().sendVerificationEmail(email, code);
                req.getSession().setAttribute("pendingEmail", email);

                resp.sendRedirect("verify");
                break;

            case "/login":
                email = req.getParameter("email");
                password = req.getParameter("password");

                Optional<Member> memberOpt = memberService.connectMember(email, password);
                if (memberOpt.isPresent()) {
                    req.getSession().setAttribute("member", memberOpt.get());

                    String redirectUrl = req.getParameter("redirect");
                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        // Ajouter le context path si ce n'est pas déjà fait
                        if (!redirectUrl.startsWith(req.getContextPath())) {
                            redirectUrl = req.getContextPath() + redirectUrl;
                        }
                        System.out.println("🔄 Redirection après login vers: " + redirectUrl);
                        resp.sendRedirect(redirectUrl);
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/home");
                    }
                } else {
                    context.setVariable("pageTitle", "Connexion");
                    context.setVariable("error", "Identifiants incorrects");
                    context.setVariable("page", "login");
                    engine.process("login", context, resp.getWriter());
                }
                break;

            case "/verify":
                String userInputCode = req.getParameter("code");
                String pendingEmail = (String) req.getSession().getAttribute("pendingEmail");

                if (pendingEmail == null) {
                    context.setVariable("error", "Session expirée. Veuillez vous réinscrire.");
                    context.setVariable("page", "verify");
                    engine.process("verify", context, resp.getWriter());
                    return;
                }

                // Récupérer le vrai code stocké
                String expectedCode = VerificationCodeStorage.getCode(pendingEmail);

                if (expectedCode != null && expectedCode.equals(userInputCode)) {
                    // Vérification réussie
                    req.getSession().removeAttribute("pendingEmail");
                    VerificationCodeStorage.removeCode(pendingEmail);

                    context.setVariable("success", "Votre adresse email a été vérifiée !");
                    context.setVariable("page", "login");
                    engine.process("login", context, resp.getWriter());
                } else {
                    // Mauvais code
                    context.setVariable("error", "Code de vérification invalide.");
                    context.setVariable("page", "verify");
                    engine.process("verify", context, resp.getWriter());
                }
                break;

        }
    }
}
