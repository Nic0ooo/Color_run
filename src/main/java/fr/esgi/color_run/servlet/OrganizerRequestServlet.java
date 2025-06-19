package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.AssociationService;
import fr.esgi.color_run.service.OrganizerRequestService;
import fr.esgi.color_run.service.impl.AssociationServiceImpl;
import fr.esgi.color_run.service.impl.OrganizerRequestServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;

@WebServlet(name = "OrganizerRequestServlet", urlPatterns = {"/organizer-request"})
public class OrganizerRequestServlet extends HttpServlet {

    private final OrganizerRequestService organizerRequestService = new OrganizerRequestServiceImpl();
    private final AssociationService associationService = new AssociationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("🔍 OrganizerRequestServlet - doGet() appelé");

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        // Vérifier que l'utilisateur est connecté
        Member member = (Member) req.getSession().getAttribute("member");
        System.out.println("🔍 Membre en session: " + (member != null ? member.getEmail() + " (ID: " + member.getId() + ")" : "null"));

        if (member == null) {
            System.out.println("❌ Utilisateur non connecté - redirection vers login");
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + req.getRequestURI());
            return;
        }

        // Debug du rôle
        System.out.println("🔍 Rôle du membre: " + member.getRole());

        // Vérifier s'il a déjà une demande en cours
        boolean hasPendingRequest = false;
        try {
            hasPendingRequest = organizerRequestService.hasActivePendingRequest(member.getId());
            System.out.println("🔍 A une demande en cours: " + hasPendingRequest);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification des demandes en cours:");
            e.printStackTrace();
        }

        // Récupérer les associations
        try {
            context.setVariable("associations", associationService.getAllAssociations());
            System.out.println("✅ Associations chargées");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des associations:");
            e.printStackTrace();
            context.setVariable("associations", java.util.Collections.emptyList());
        }

        context.setVariable("member", member);
        context.setVariable("hasPendingRequest", hasPendingRequest);
        context.setVariable("pageTitle", "Devenir organisateur");
        context.setVariable("page", "organizer-request");

        System.out.println("✅ Rendu de la page organizer-request");
        engine.process("organizer-request", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("🔍 OrganizerRequestServlet - doPost() appelé");

        // Debug des paramètres reçus
        System.out.println("🔍 Paramètres reçus:");
        req.getParameterMap().forEach((key, values) -> {
            if (!"motivation".equals(key)) { // Ne pas logger la motivation complète
                System.out.println("  - " + key + ": " + String.join(", ", values));
            } else {
                System.out.println("  - " + key + ": [" + values[0].length() + " caractères]");
            }
        });

        // Vérifier que l'utilisateur est connecté
        Member member = (Member) req.getSession().getAttribute("member");
        if (member == null) {
            System.out.println("❌ Utilisateur non connecté dans doPost - redirection vers login");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        System.out.println("🔍 Membre connecté: " + member.getEmail() + " (ID: " + member.getId() + ", Rôle: " + member.getRole() + ")");

        try {
            String motivation = req.getParameter("motivation");
            String associationType = req.getParameter("associationType");

            System.out.println("🔍 Type d'association: " + associationType);
            System.out.println("🔍 Longueur motivation: " + (motivation != null ? motivation.length() : "null"));

            // Validation
            if (motivation == null || motivation.trim().length() < 50) {
                System.out.println("❌ Validation échouée: motivation trop courte");
                resp.sendRedirect(req.getContextPath() + "/organizer-request?error=invalid_data");
                return;
            }

            // Vérifier que l'utilisateur peut soumettre une demande
            if (!organizerRequestService.canMemberSubmitRequest(member.getId())) {
                System.out.println("❌ Le membre ne peut pas soumettre de demande");
                resp.sendRedirect(req.getContextPath() + "/organizer-request?error=pending_request");
                return;
            }

            Long existingAssociationId = null;

            if ("existing".equals(associationType)) {
                String assocIdStr = req.getParameter("existingAssociationId");
                System.out.println("🔍 Association existante ID string: " + assocIdStr);
                if (assocIdStr != null && !assocIdStr.isEmpty()) {
                    try {
                        existingAssociationId = Long.parseLong(assocIdStr);
                        System.out.println("✅ Association existante ID: " + existingAssociationId);
                    } catch (NumberFormatException e) {
                        System.err.println("❌ Erreur parsing association ID: " + assocIdStr);
                    }
                }
            } else if ("new".equals(associationType)) {
                System.out.println("📝 Demande de nouvelle association (pas encore implémenté)");
                // TODO: Gérer la création d'une nouvelle association
            }

            // Créer la demande
            System.out.println("📝 Création de la demande...");
            organizerRequestService.submitRequest(member.getId(), motivation.trim(), existingAssociationId);
            System.out.println("✅ Demande créée avec succès");

            resp.sendRedirect(req.getContextPath() + "/organizer-request?success=request_sent");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la soumission de la demande organisateur:");
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/organizer-request?error=server_error");
        }
    }
}