package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.impl.MemberServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/login", "/register"})
public class MemberServlet extends HttpServlet {

    private final MemberService memberService = new MemberServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        switch (req.getServletPath()) {
            case "/login":
                context.setVariable("pageTitle", "Connexion");
                engine.process("login", context, resp.getWriter());
                break;
            case "/register":
                context.setVariable("pageTitle", "Inscription");
                engine.process("register", context, resp.getWriter());
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

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

                newMember.setPositionLatitude(48.8566);
                newMember.setPositionLongitude(2.3522);

                memberService.createMember(newMember);
                resp.sendRedirect("login");
                break;

            case "/login":
                email = req.getParameter("email");
                password = req.getParameter("password");

                Optional<Member> memberOpt = memberService.connectMember(email, password);

                if (memberOpt.isPresent()) {
                    req.getSession().setAttribute("member", memberOpt.get());
                    resp.sendRedirect("home");
                } else {
                    TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
                    WebContext context = new WebContext(
                            ThymeleafConfiguration.getApplication().buildExchange(req, resp));
                    context.setVariable("error", "Identifiants incorrects");
                    engine.process("login", context, resp.getWriter());
                }
                break;
        }
    }
}
