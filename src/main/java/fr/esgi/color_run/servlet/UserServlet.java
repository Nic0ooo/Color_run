package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.User;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import java.io.IOException;
import java.util.List;

public class UserServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        // Initialiser le service utilisateur
        this.userService = new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuration de la réponse
        response.setContentType("text/html;charset=UTF-8");

        // Récupérer le moteur Thymeleaf
        TemplateEngine templateEngine = ThymeleafConfiguration.getTemplateEngine();
        IWebExchange exchange = ThymeleafConfiguration.getApplication()
                .buildExchange(request, response);
        WebContext context = new WebContext(exchange);

        // Déterminer l'action à effectuer
        String action = request.getParameter("action");

        if (action == null) {
            // Afficher la liste des utilisateurs
            listUsers(context, templateEngine, response);
        } else if (action.equals("add")) {
            // Afficher le formulaire d'ajout
            showAddForm(context, templateEngine, response);
        } else if (action.equals("edit")) {
            // Afficher le formulaire d'édition
            showEditForm(request, context, templateEngine, response);
        } else if (action.equals("view")) {
            // Afficher les détails d'un utilisateur
            viewUser(request, context, templateEngine, response);
        } else {
            // Action non reconnue, rediriger vers la liste
            response.sendRedirect(request.getContextPath() + "/users");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/users");
            return;
        }

        if (action.equals("add")) {
            // Ajouter un nouvel utilisateur
            createUser(request, response);
        } else if (action.equals("update")) {
            // Mettre à jour un utilisateur existant
            updateUser(request, response);
        } else if (action.equals("delete")) {
            // Supprimer un utilisateur
            deleteUser(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/users");
        }
    }

    private void listUsers(WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        List<User> users = userService.getAllUsers();
        context.setVariable("users", users);
        context.setVariable("pageTitle", "Liste des utilisateurs");
        templateEngine.process("users", context, response.getWriter());
    }

    private void showAddForm(WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        User user = new User();
        context.setVariable("user", user);
        context.setVariable("roles", Role.values());
        context.setVariable("pageTitle", "Ajouter un utilisateur");
        context.setVariable("isNew", true);
        templateEngine.process("user-form", context, response.getWriter());
    }

    private void showEditForm(HttpServletRequest request, WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            User user = userService.getUserById(id);
            if (user != null) {
                context.setVariable("user", user);
                context.setVariable("roles", Role.values());
                context.setVariable("pageTitle", "Modifier l'utilisateur");
                context.setVariable("isNew", false);
                templateEngine.process("user-form", context, response.getWriter());
            } else {
                response.sendRedirect(request.getContextPath() + "/users");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/users");
        }
    }

    private void viewUser(HttpServletRequest request, WebContext context, TemplateEngine templateEngine, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            User user = userService.getUserById(id);
            if (user != null) {
                context.setVariable("user", user);
                context.setVariable("pageTitle", "Détails de l'utilisateur");
                templateEngine.process("user-details", context, response.getWriter());
            } else {
                response.sendRedirect(request.getContextPath() + "/users");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/users");
        }
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = new User();
        populateUserFromRequest(user, request);
        userService.saveUser(user);
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            User user = userService.getUserById(id);
            if (user != null) {
                populateUserFromRequest(user, request);
                userService.updateUser(user);
            }
        } catch (NumberFormatException e) {
            // Gérer l'erreur
        }
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            userService.deleteUser(id);
        } catch (NumberFormatException e) {
            // Gérer l'erreur
        }
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void populateUserFromRequest(User user, HttpServletRequest reques