package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.business.RequestType;
import fr.esgi.color_run.business.OrganizerRequest;
import fr.esgi.color_run.business.RequestStatus;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.AssociationService;
import fr.esgi.color_run.service.OrganizerRequestService;
import fr.esgi.color_run.service.Association_memberService;
import fr.esgi.color_run.service.impl.AssociationServiceImpl;
import fr.esgi.color_run.service.impl.OrganizerRequestServiceImpl;
import fr.esgi.color_run.service.impl.Association_memberServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "OrganizerRequestServlet", urlPatterns = {"/organizer-request"})
public class OrganizerRequestServlet extends HttpServlet {

    private final OrganizerRequestService organizerRequestService = new OrganizerRequestServiceImpl();
    private final AssociationService associationService = new AssociationServiceImpl();
    private final Association_memberService associationMemberService = new Association_memberServiceImpl();

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

        System.out.println("🔍 Rôle du membre: " + member.getRole());

        // Vérifier s'il a déjà une demande en cours
        boolean hasPendingRequest = false;
        OrganizerRequest lastRejectedRequest = null;

        try {
            hasPendingRequest = organizerRequestService.hasActivePendingRequest(member.getId());
            System.out.println("🔍 A une demande en cours: " + hasPendingRequest);

            // Vérifier s'il a une demande récemment refusée
            if (!hasPendingRequest) {
                var memberRequests = organizerRequestService.getRequestsByMember(member.getId());
                lastRejectedRequest = memberRequests.stream()
                        .filter(request -> request.getStatus() == RequestStatus.REJECTED)
                        .findFirst()
                        .orElse(null);

                if (lastRejectedRequest != null) {
                    System.out.println("🔍 Dernière demande refusée trouvée: " + lastRejectedRequest.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification des demandes:");
            e.printStackTrace();
        }

        // Récupérer les associations
        try {
            List<Association> allAssociations = associationService.getAllAssociations();

            if (member.getRole() == Role.ORGANIZER) {
                // Pour les organisateurs, charger leurs associations actuelles
                var currentAssociations = associationMemberService.getAssociationsByOrganizer(member.getId());
                context.setVariable("currentAssociations", currentAssociations);

                // Pour les associations disponibles, utiliser la méthode spécialisée
                var availableAssociations = associationMemberService.getAvailableAssociationForMember(member.getId());
                context.setVariable("availableAssociations", availableAssociations);

                System.out.println("✅ Organisateur: " + currentAssociations.size() + " associations actuelles, " +
                        availableAssociations.size() + " disponibles");
            } else {
                // Pour les participants, toutes les associations sont disponibles
                context.setVariable("associations", allAssociations);
                context.setVariable("availableAssociations", allAssociations);
                System.out.println("✅ Participant: " + allAssociations.size() + " associations chargées");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des associations:");
            e.printStackTrace();
            context.setVariable("associations", java.util.Collections.emptyList());
            context.setVariable("availableAssociations", java.util.Collections.emptyList());
            context.setVariable("currentAssociations", java.util.Collections.emptyList());
        }

        context.setVariable("member", member);
        context.setVariable("hasPendingRequest", hasPendingRequest);
        context.setVariable("lastRejectedRequest", lastRejectedRequest);
        context.setVariable("pageTitle", member.getRole() == Role.ORGANIZER ?
                "Gérer mes associations" : "Devenir organisateur");
        context.setVariable("page", "organizer-request");

        System.out.println("✅ Rendu de la page organizer-request");
        engine.process("organizer-request", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("🔍 OrganizerRequestServlet - doPost() appelé");

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
            String requestTypeParam = req.getParameter("requestType");

            System.out.println("🔍 Type d'association: " + associationType);
            System.out.println("🔍 Type de demande: " + requestTypeParam);
            System.out.println("🔍 Longueur motivation: " + (motivation != null ? motivation.length() : "null"));

            // Validation de base
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

            // Déterminer le type de demande
            RequestType requestType = RequestType.BECOME_ORGANIZER;
            if (member.getRole() == Role.ORGANIZER) {
                if ("existing".equals(associationType)) {
                    requestType = RequestType.JOIN_ASSOCIATION;
                } else if ("new".equals(associationType)) {
                    requestType = RequestType.CREATE_ASSOCIATION;
                }
            }

            System.out.println("🔍 Type de demande déterminé: " + requestType);

            Long existingAssociationId = null;
            String newAssociationName = null;
            String newAssociationEmail = null;

            if ("existing".equals(associationType)) {
                String assocIdStr = req.getParameter("existingAssociationId");
                System.out.println("🔍 Association existante ID string: " + assocIdStr);
                if (assocIdStr != null && !assocIdStr.isEmpty()) {
                    try {
                        existingAssociationId = Long.parseLong(assocIdStr);
                        System.out.println("✅ Association existante ID: " + existingAssociationId);
                    } catch (NumberFormatException e) {
                        System.err.println("❌ Erreur parsing association ID: " + assocIdStr);
                        resp.sendRedirect(req.getContextPath() + "/organizer-request?error=invalid_data");
                        return;
                    }
                }
            } else if ("new".equals(associationType)) {
                newAssociationName = req.getParameter("assocName");
                newAssociationEmail = req.getParameter("assocEmail");
                System.out.println("🔍 Nouvelle association: " + newAssociationName + " - " + newAssociationEmail);

                if (newAssociationName == null || newAssociationName.trim().isEmpty()) {
                    System.out.println("❌ Nom d'association manquant");
                    resp.sendRedirect(req.getContextPath() + "/organizer-request?error=invalid_data");
                    return;
                }

                if (newAssociationEmail == null || newAssociationEmail.trim().isEmpty()) {
                    System.out.println("❌ Email d'association manquant");
                    resp.sendRedirect(req.getContextPath() + "/organizer-request?error=invalid_data");
                    return;
                }

                // NOUVELLE FONCTIONNALITÉ: Vérifier si l'association existe déjà
                try {
                    if (associationService.existsByName(newAssociationName.trim())) {
                        System.out.println("❌ Association avec ce nom existe déjà: " + newAssociationName);
                        resp.sendRedirect(req.getContextPath() + "/organizer-request?error=association_name_exists");
                        return;
                    }

                    if (associationService.existsByEmail(newAssociationEmail.trim().toLowerCase())) {
                        System.out.println("❌ Association avec cet email existe déjà: " + newAssociationEmail);
                        resp.sendRedirect(req.getContextPath() + "/organizer-request?error=association_email_exists");
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de la vérification d'existence de l'association:");
                    e.printStackTrace();
                    resp.sendRedirect(req.getContextPath() + "/organizer-request?error=server_error");
                    return;
                }
            }

            // Créer la demande avec les informations d'association
            System.out.println("📝 Création de la demande...");

            if ("new".equals(associationType) && newAssociationName != null) {
                // Demande avec nouvelle association
                organizerRequestService.submitRequestWithNewAssociation(
                        member.getId(),
                        requestType,
                        motivation.trim(),
                        req.getParameter("assocName"),
                        req.getParameter("assocEmail"),
                        req.getParameter("assocDescription"),
                        req.getParameter("assocWebsiteLink"),
                        req.getParameter("assocPhone"),
                        req.getParameter("assocAddress"),
                        req.getParameter("assocZipCode"),
                        req.getParameter("assocCity")
                );
                System.out.println("✅ Demande avec nouvelle association créée");
                resp.sendRedirect(req.getContextPath() + "/organizer-request?success=association_created");
            } else {
                // Demande standard (avec ou sans association existante)
                organizerRequestService.submitRequest(member.getId(), requestType, motivation.trim(), existingAssociationId);
                System.out.println("✅ Demande standard créée");

                if (member.getRole() == Role.ORGANIZER) {
                    resp.sendRedirect(req.getContextPath() + "/organizer-request?success=association_request_sent");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/organizer-request?success=request_sent");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la soumission de la demande organisateur:");
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/organizer-request?error=server_error");
        }
    }
}