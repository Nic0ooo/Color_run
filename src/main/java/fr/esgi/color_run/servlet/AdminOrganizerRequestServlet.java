package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.*;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.OrganizerRequestService;
import fr.esgi.color_run.service.AssociationService;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.Association_memberService;
import fr.esgi.color_run.service.impl.MemberServiceImpl;
import fr.esgi.color_run.service.impl.OrganizerRequestServiceImpl;
import fr.esgi.color_run.service.impl.AssociationServiceImpl;
import fr.esgi.color_run.service.impl.Association_memberServiceImpl;
import fr.esgi.color_run.util.RepositoryFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/admin-organizer-requests", "/admin-organizer-requests/approve", "/admin-organizer-requests/reject"})
public class AdminOrganizerRequestServlet extends HttpServlet {

    // Services utilisant le système optimisé
    private final OrganizerRequestService organizerRequestService;
    private final AssociationService associationService;
    private final Association_memberService associationMemberService;
    private final MemberService memberService;

    public AdminOrganizerRequestServlet() {
        // Utilisation du singleton pour éviter les créations multiples
        this.organizerRequestService = new OrganizerRequestServiceImpl();
        this.associationService = new AssociationServiceImpl();
        this.associationMemberService = new Association_memberServiceImpl();
        this.memberService = new MemberServiceImpl();

        System.out.println("✅ AdminOrganizerRequestServlet initialisé avec services optimisés");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("🔍 AdminOrganizerRequestServlet - doGet() appelé pour: " + req.getServletPath());

        // Vérifier que l'utilisateur est admin
        Member member = (Member) req.getSession().getAttribute("member");
        if (member == null || member.getRole() != Role.ADMIN) {
            System.out.println("❌ Accès refusé - utilisateur non admin");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        System.out.println("✅ Admin connecté: " + member.getEmail());

        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp));

        try {
            context.setVariable("member", member);

            // Charger les demandes de façon optimisée (avec cache)
            var pendingRequests = organizerRequestService.getPendingRequests();
            var allRequests = organizerRequestService.getAllRequests();

            System.out.println("🔍 Demandes en attente trouvées: " + pendingRequests.size());
            System.out.println("🔍 Total demandes trouvées: " + allRequests.size());

            // Enrichir avec les informations des membres (optimisé)
            Map<Long, Member> membersMap = loadMembersForRequestsOptimized(allRequests, pendingRequests);
            context.setVariable("membersMap", membersMap);

            Map<Long, Association> associationsMap = loadAssociationsForRequestsOptimized(allRequests);
            context.setVariable("associationsMap", associationsMap);

            // Calculer les compteurs de façon sécurisée
            long approvedCount = 0;
            long rejectedCount = 0;

            if (allRequests != null) {
                for (var request : allRequests) {
                    if (request != null && request.getStatus() != null) {
                        switch (request.getStatus().name()) {
                            case "APPROVED":
                                approvedCount++;
                                break;
                            case "REJECTED":
                                rejectedCount++;
                                break;
                        }
                    }
                }
            }

            System.out.println("📊 Statistiques: " + approvedCount + " approuvées, " + rejectedCount + " refusées");

            context.setVariable("pendingRequests", pendingRequests != null ? pendingRequests : java.util.Collections.emptyList());
            context.setVariable("allRequests", allRequests != null ? allRequests : java.util.Collections.emptyList());
            context.setVariable("approvedCount", approvedCount);
            context.setVariable("rejectedCount", rejectedCount);
            context.setVariable("pageTitle", "Gestion des demandes organisateur");
            context.setVariable("page", "admin-organizer-requests");

            System.out.println("✅ Données chargées pour la page admin");

            engine.process("admin-organizer-requests", context, resp.getWriter());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de la page admin:");
            e.printStackTrace();
            throw new ServletException("Erreur lors du chargement des demandes", e);
        }
    }

    /**
     * Version optimisée pour charger les membres en batch
     */
    private Map<Long, Member> loadMembersForRequestsOptimized(List<OrganizerRequest> allRequests, List<OrganizerRequest> pendingRequests) {
        Map<Long, Member> membersMap = new ConcurrentHashMap<>();

        // Collecter tous les IDs uniques de membres des deux listes
        Set<Long> memberIds = new HashSet<>();

        if (allRequests != null) {
            allRequests.stream()
                    .filter(request -> request != null && request.getMemberId() != null)
                    .map(OrganizerRequest::getMemberId)
                    .forEach(memberIds::add);
        }

        if (pendingRequests != null) {
            pendingRequests.stream()
                    .filter(request -> request != null && request.getMemberId() != null)
                    .map(OrganizerRequest::getMemberId)
                    .forEach(memberIds::add);
        }

        // Charger tous les membres
        for (Long memberId : memberIds) {
            try {
                memberService.getMember(memberId).ifPresent(member ->
                        membersMap.put(memberId, member));
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du chargement du membre " + memberId + ": " + e.getMessage());
            }
        }

        System.out.println("✅ " + membersMap.size() + " membres chargés");
        return membersMap;
    }

    /**
     * Version optimisée pour charger les associations en batch
     */
    private Map<Long, Association> loadAssociationsForRequestsOptimized(List<OrganizerRequest> allRequests) {
        Map<Long, Association> associationsMap = new ConcurrentHashMap<>();

        if (allRequests == null || allRequests.isEmpty()) {
            return associationsMap;
        }

        // Collecter tous les IDs uniques d'associations
        var associationIds = allRequests.stream()
                .filter(request -> request != null && request.getExistingAssociationId() != null)
                .map(OrganizerRequest::getExistingAssociationId)
                .collect(Collectors.toSet());

        // Charger toutes les associations de façon optimisée
        for (Long associationId : associationIds) {
            try {
                associationService.findById(associationId).ifPresent(association ->
                        associationsMap.put(associationId, association));
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du chargement de l'association " + associationId + ": " + e.getMessage());
            }
        }

        System.out.println("✅ " + associationsMap.size() + " associations chargées");
        return associationsMap;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("🔍 AdminOrganizerRequestServlet - doPost() appelé pour: " + req.getServletPath());

        // Vérifier que l'utilisateur est admin
        Member member = (Member) req.getSession().getAttribute("member");
        if (member == null || member.getRole() != Role.ADMIN) {
            System.out.println("❌ Accès refusé - utilisateur non admin");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();
        System.out.println("🔍 Action demandée: " + path);

        try {
            String requestIdStr = req.getParameter("requestId");
            String comment = req.getParameter("comment");
            String createAssociationStr = req.getParameter("createAssociation");

            System.out.println("🔍 Request ID: " + requestIdStr);
            System.out.println("🔍 Commentaire: " + (comment != null ? comment.length() + " caractères" : "null"));
            System.out.println("🔍 Créer association: " + createAssociationStr);

            if (requestIdStr == null || requestIdStr.isEmpty()) {
                System.out.println("❌ Request ID manquant");
                resp.sendRedirect(req.getContextPath() + "/admin-organizer-requests?error=missing_id");
                return;
            }

            Long requestId = Long.parseLong(requestIdStr);

            if (path.endsWith("/approve")) {
                System.out.println("✅ Approbation de la demande " + requestId);

                boolean shouldCreateAssociation = "on".equals(createAssociationStr);
                var request = organizerRequestService.approveRequest(requestId, member.getId(), comment);

                // S'assurer que le membre est bien ORGANIZER avant de continuer
                Optional<Member> memberOpt = memberService.getMember(request.getMemberId());
                if (memberOpt.isPresent()) {
                    Member requestMember = memberOpt.get();

                    if (requestMember.getRole() != Role.ORGANIZER) {
                        requestMember.setRole(Role.ORGANIZER);
                        memberService.updateMember(requestMember.getId(), requestMember);
                        System.out.println("🔄 Mise à jour du rôle du membre " + requestMember.getEmail() + " en ORGANIZER");
                    }

                    // Traitement spécial selon le type de demande
                    if (request.getRequestType() != null) {
                        switch (request.getRequestType()) {
                            case JOIN_ASSOCIATION:
                                if (request.getExistingAssociationId() != null) {
                                    System.out.println("🔗 Ajout de l'organisateur à l'association " + request.getExistingAssociationId());
                                    try {
                                        associationMemberService.addOrganizerToAssociation(
                                                request.getMemberId(),
                                                request.getExistingAssociationId()
                                        );
                                        System.out.println("✅ Organisateur ajouté à l'association");
                                    } catch (Exception e) {
                                        System.err.println("❌ Erreur lors de l'ajout à l'association:");
                                        e.printStackTrace();
                                    }
                                }
                                break;

                            case CREATE_ASSOCIATION:
                                if (shouldCreateAssociation && request.hasNewAssociation()) {
                                    System.out.println("🏢 Création de nouvelle association: " + request.getNewAssociationName());
                                    // vérifier que l'association n'existe pas déjà
                                    if (associationService.existsByName(request.getNewAssociationName())) {
                                        System.out.println("❌ L'association " + request.getNewAssociationName() + " existe déjà");
                                        req.getSession().setAttribute("error", "L'Association existe déjà. Veuillez refaire une demande pour la rejoindre.");
                                        resp.sendRedirect(req.getContextPath() + "/admin-organizer-requests?error=association_exists");
                                        return;
                                    }
                                    if (associationService.existsByEmail(request.getNewAssociationEmail())) {
                                        System.out.println("❌ L'association avec l'email " + request.getNewAssociationEmail() + " existe déjà");
                                        req.getSession().setAttribute("error", "Une association avec ce mail existe déjà. Veuillez refaire une demande pour la rejoindre.");
                                        resp.sendRedirect(req.getContextPath() + "/admin-organizer-requests?error=association_email_exists");
                                        return;
                                    }

                                    try {
                                        // Créer l'association
                                        Long newAssociationId = associationService.createAssociation(
                                                request.getNewAssociationName(),
                                                request.getNewAssociationEmail(),
                                                request.getNewAssociationDescription(),
                                                request.getNewAssociationWebsiteLink(),
                                                request.getNewAssociationPhone(),
                                                request.getNewAssociationAddress(),
                                                request.getNewAssociationZipCode(),
                                                request.getNewAssociationCity()
                                        );

                                        // Ajouter le membre à la nouvelle association
                                        associationMemberService.addOrganizerToAssociation(
                                                request.getMemberId(),
                                                newAssociationId
                                        );

                                        System.out.println("✅ Association créée avec ID " + newAssociationId +
                                                " et membre ajouté");
                                    } catch (Exception e) {
                                        System.err.println("❌ Erreur lors de la création de l'association:");
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            case BECOME_ORGANIZER:
                                // dans ce cas, changement de rôle fait dans le service.
                                break;
                        }
                    }
                }
                req.getSession().setAttribute("member", member);
                resp.sendRedirect(req.getContextPath() + "/admin-organizer-requests?success=approved");

            } else if (path.endsWith("/reject")) {
                System.out.println("❌ Rejet de la demande " + requestId);
                organizerRequestService.rejectRequest(requestId, member.getId(), comment);
                req.getSession().setAttribute("member", member);
                resp.sendRedirect(req.getContextPath() + "/admin-organizer-requests?success=rejected");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement de la demande:");
            e.printStackTrace();
            req.getSession().setAttribute("member", member);
            resp.sendRedirect(req.getContextPath() + "/admin-organizer-requests?error=processing_failed");
        }
    }
}