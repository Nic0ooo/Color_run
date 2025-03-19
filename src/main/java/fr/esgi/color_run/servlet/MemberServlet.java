package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/members")
public class MemberServlet extends HttpServlet {

    // Liste pour stocker les utilisateurs (en mémoire, pour ce tutoriel)
    private List<Member> members = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        // Ajouter quelques utilisateurs pour les tests
        Member user1 = new Member();
        user1.setName("Dupont");
        user1.setFirstname("Jean");
        user1.setEmail("jean.dupont@example.com");
        user1.setPassword("password123");
        user1.setCity("Paris");

        Member user2 = new Member();
        user2.setName("Martin");
        user2.setFirstname("Sophie");
        user2.setEmail("sophie.martin@example.com");
        user2.setPassword("password456");
        user2.setCity("Lyon");

        members.add(user1);
        members.add(user2);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuration de la réponse
        response.setContentType("text/html;charset=UTF-8");

        // Récupérer le moteur Thymeleaf et créer le contexte
        TemplateEngine templateEngine = ThymeleafConfiguration.getTemplateEngine();
        // Utilisation de l'application JakartaServletWebApplication
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(request, response));

        // Déterminer l'action à effectuer
        String action = request.getParameter("action");

        if (action == null) {
            // Afficher la liste des utilisateurs
            listMembers(context, templateEngine, response);
        } else if (action.equals("add")) {
            // Afficher le formulaire d'ajout
            showAddForm(context, templateEngine, response);
        } else if (action.equals("edit")) {
            // Afficher le formulaire d'édition
            showEditForm(request, context, templateEngine, response);
        } else if (action.equals("view")) {
            // Afficher les détails d'un utilisateur
            viewUser(request, context, templateEngine, response);
        } else if (action.equals("delete")) {
            // Supprimer un utilisateur
            deleteUser(request, response);
        } else {
            // Action non reconnue, rediriger vers la liste
            response.sendRedirect(request.getContextPath() + "/members");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/members");
            return;
        }

        if (action.equals("add")) {
            // Ajouter un nouvel utilisateur
            createUser(request, response);
        } else if (action.equals("update")) {
            // Mettre à jour un utilisateur existant
            updateUser(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/members");
        }
    }

    private void listMembers(WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        context.setVariable("members", members);
        context.setVariable("pageTitle", "Liste des utilisateurs");
        templateEngine.process("members", context, response.getWriter());
    }

    private void showAddForm(WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        Member member = new Member();
        context.setVariable("member", member);
        context.setVariable("roles", Role.values());
        context.setVariable("pageTitle", "Ajouter un utilisateur");
        context.setVariable("isNew", true);
        templateEngine.process("member-form", context, response.getWriter());
    }

    private void showEditForm(HttpServletRequest request, WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Member member = findUserById(id);

            if (member != null) {
                context.setVariable("member", member);
                context.setVariable("roles", Role.values());
                context.setVariable("pageTitle", "Modifier l'utilisateur");
                context.setVariable("isNew", false);
                templateEngine.process("member-form", context, response.getWriter());
            } else {
                response.sendRedirect(request.getContextPath() + "/members");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/members");
        }
    }

    private void viewUser(HttpServletRequest request, WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Member member = findUserById(id);

            if (member != null) {
                context.setVariable("member", member);
                context.setVariable("pageTitle", "Détails de l'utilisateur");
                templateEngine.process("member-details", context, response.getWriter());
            } else {
                response.sendRedirect(request.getContextPath() + "/members");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/members");
        }
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = new Member();
        populateUserFromRequest(member, request);
        members.add(member);
        response.sendRedirect(request.getContextPath() + "/members");
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Member member = findUserById(id);

            if (member != null) {
                populateUserFromRequest(member, request);
            }
        } catch (NumberFormatException e) {
            // Gérer l'erreur
        }
        response.sendRedirect(request.getContextPath() + "/members");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            members.removeIf(member -> member.getId().equals(id));
        } catch (NumberFormatException e) {
            // Gérer l'erreur
        }
        response.sendRedirect(request.getContextPath() + "/members");
    }

    private Member findUserById(Long id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void populateUserFromRequest(Member member, HttpServletRequest request) {
        // Récupérer les paramètres du formulaire
        String name = request.getParameter("name");
        String firstname = request.getParameter("firstname");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phoneStr = request.getParameter("phoneNumber");
        String address = request.getParameter("address");
        String city = request.getParameter("city");
        String zipCodeStr = request.getParameter("zipCode");
        String latitudeStr = request.getParameter("positionLatitude");
        String longitudeStr = request.getParameter("positionLongitude");
        String roleStr = request.getParameter("role");

        // Mettre à jour l'utilisateur
        member.setName(name);
        member.setFirstname(firstname);
        member.setEmail(email);

        // Ne mettre à jour le mot de passe que s'il est fourni
        if (password != null && !password.isEmpty()) {
            member.setPassword(password);
        }

        // Convertir et définir les valeurs numériques
        try {
            if (phoneStr != null && !phoneStr.isEmpty()) {
                member.setPhoneNumber(Integer.parseInt(phoneStr));
            }

            if (zipCodeStr != null && !zipCodeStr.isEmpty()) {
                member.setZipCode(Integer.parseInt(zipCodeStr));
            }

            if (latitudeStr != null && !latitudeStr.isEmpty()) {
                member.setPositionLatitude(Double.parseDouble(latitudeStr));
            }

            if (longitudeStr != null && !longitudeStr.isEmpty()) {
                member.setPositionLongitude(Double.parseDouble(longitudeStr));
            }

            if (roleStr != null && !roleStr.isEmpty()) {
                member.setRole(Role.valueOf(roleStr));
            }
        } catch (IllegalArgumentException e) {
            // Gérer les erreurs de conversion
        }

        member.setAddress(address);
        member.setCity(city);
    }
}