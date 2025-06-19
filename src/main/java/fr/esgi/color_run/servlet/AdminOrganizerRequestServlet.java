package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.OrganizerRequestService;
import fr.esgi.color_run.service.Association_memberService;
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

@WebServlet(urlPatterns = {"/admin/organizer-requests", "/admin/organizer-request/approve", "/admin/organizer-request/reject"})
public class AdminOrganizerRequestServlet extends HttpServlet {

    private final OrganizerRequestService organizerRequestService = new OrganizerRequestServiceImpl();
    private final Association_memberService associationMemberService = new Association_memberServiceImpl();

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
            context.setVariable("pendingRequests", organizerRequestService.getPendingRequests());
            context.setVariable("allRequests", organizerRequestService.getAllRequests());
            context.setVariable("pageTitle", "Gestion des demandes organisateur");
            context.setVariable("page", "admin-organizer-requests");

            System.out.println("✅ Données chargées pour la page admin");
            engine.process("admin/organizer-requests", context, resp.getWriter());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de la page admin:");
            e.printStackTrace();
            throw new ServletException("Erreur lors du chargement des demandes", e);
        }
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

            System.out.println("🔍 Request ID: " + requestIdStr);
            System.out.println("🔍 Commentaire: " + (comment != null ? comment.length() + " caractères" : "null"));

            if (requestIdStr == null || requestIdStr.isEmpty()) {
                System.out.println("❌ Request ID manquant");
                resp.sendRedirect(req.getContextPath() + "/admin/organizer-requests?error=missing_id");
                return;
            }

            Long requestId = Long.parseLong(requestIdStr);

            if (path.endsWith("/approve")) {
                System.out.println("✅ Approbation de la demande " + requestId);
                var request = organizerRequestService.approveRequest(requestId, member.getId(), comment);

                // Si une association était demandée, ajouter l'organisateur à l'association
                if (request.getExistingAssociationId() != null) {
                    System.out.println("🔗 Ajout de l'organisateur à l'association " + request.getExistingAssociationId());
                    try {
                        associationMemberService.addOrganizerToAssociation(request.getMemberId(), request.getExistingAssociationId());
                        System.out.println("✅ Organisateur ajouté à l'association");
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors de l'ajout à l'association:");
                        e.printStackTrace();
                        // Ne pas faire échouer toute la demande pour ça
                    }
                }

                resp.sendRedirect(req.getContextPath() + "/admin/organizer-requests?success=approved");

            } else if (path.endsWith("/reject")) {
                System.out.println("❌ Rejet de la demande " + requestId);
                organizerRequestService.rejectRequest(requestId, member.getId(), comment);
                resp.sendRedirect(req.getContextPath() + "/admin/organizer-requests?success=rejected");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement de la demande:");
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/organizer-requests?error=processing_failed");
        }
    }
}