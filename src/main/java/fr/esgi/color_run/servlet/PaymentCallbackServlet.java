package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.repository.impl.Course_memberRepositoryImpl;
import fr.esgi.color_run.service.Course_memberService;
import fr.esgi.color_run.service.PaiementService;
import fr.esgi.color_run.service.impl.Course_memberServiceImpl;
import fr.esgi.color_run.service.impl.PaiementServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Random;

@WebServlet(urlPatterns = {"/payment-success", "/payment-cancel"})
public class PaymentCallbackServlet extends HttpServlet {

    private PaiementService paiementService;
    private Course_memberService courseMemberService;

    @Override
    public void init() throws ServletException {
        super.init();
        courseMemberService = new Course_memberServiceImpl(new Course_memberRepositoryImpl());
        paiementService = new PaiementServiceImpl(courseMemberService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String servletPath = req.getServletPath();
        String courseId = req.getParameter("courseId");
        String sessionId = req.getParameter("session_id");

        System.out.println("=== PaymentCallbackServlet appelé ===");
        System.out.println("Path: " + servletPath);
        System.out.println("CourseId: " + courseId);
        System.out.println("SessionId: " + sessionId);

        // Vérifier que la réponse n'a pas déjà été envoyée
        if (resp.isCommitted()) {
            System.err.println("Réponse déjà envoyée, impossible de continuer");
            return;
        }

        try {
            if ("/payment-success".equals(servletPath)) {
                handlePaymentSuccess(req, resp, courseId, sessionId);
            } else if ("/payment-cancel".equals(servletPath)) {
                handlePaymentCancel(req, resp, courseId, sessionId);
            } else {
                if (!resp.isCommitted()) {
                    resp.sendError(404, "Page non trouvée");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur dans PaymentCallbackServlet: " + e.getMessage());
            e.printStackTrace();

            // Envoyer une réponse d'erreur seulement si possible
            if (!resp.isCommitted()) {
                resp.sendError(500, "Erreur lors du traitement du paiement");
            }
        }
    }

    private void handlePaymentSuccess(HttpServletRequest req, HttpServletResponse resp,
                                      String courseId, String sessionId)
            throws IOException {

        System.out.println("=== Traitement du paiement réussi ===");

        // Vérifier que la réponse n'est pas déjà commitée
        if (resp.isCommitted()) {
            System.err.println("Réponse déjà commitée, impossible de rediriger");
            return;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            System.err.println("Session ID manquant");
            resp.sendRedirect(req.getContextPath() + "/course-detail?id=" + courseId + "&error=missing_session");
            return;
        }

        if (courseId == null || courseId.trim().isEmpty()) {
            System.err.println("Course ID manquant");
            resp.sendRedirect(req.getContextPath() + "/courses?error=missing_course_id");
            return;
        }

        try {
            System.out.println("Tentative de confirmation du paiement...");
            Course_member confirmedRegistration = paiementService.confirmPayment(sessionId);

            // Vérifier encore une fois avant de rediriger
            if (resp.isCommitted()) {
                System.err.println("Réponse commitée pendant le traitement, impossible de rediriger");
                return;
            }

            if (confirmedRegistration != null) {
                System.out.println("Paiement confirmé et inscription validée");
                System.out.println("   - Course ID: " + confirmedRegistration.getCourseId());
                System.out.println("   - Member ID: " + confirmedRegistration.getMemberId());
                System.out.println("   - Status: " + confirmedRegistration.getRegistrationStatus());

                try {
                    if (confirmedRegistration.getBibNumber() == null || confirmedRegistration.getBibNumber().isEmpty()) {
                        String bibNumber = generateUniqueBibNumber();
                        confirmedRegistration.setBibNumber(bibNumber);

                        // Sauvegarder avec le nouveau dossard
                        courseMemberService.save(confirmedRegistration);

                        System.out.println("🎫 Dossard auto-généré: " + bibNumber +
                                " pour membre " + confirmedRegistration.getMemberId() +
                                " course " + confirmedRegistration.getCourseId());
                    } else {
                        System.out.println("Dossard déjà existant: " + confirmedRegistration.getBibNumber());
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la génération automatique du dossard: " + e.getMessage());
                    e.printStackTrace();
                    // Ne pas faire échouer le paiement pour autant - continuer
                }

                resp.sendRedirect(req.getContextPath() + "/course-detail?id=" + courseId + "&success=payment_completed");
            } else {
                System.err.println("Impossible de confirmer l'inscription");
                resp.sendRedirect(req.getContextPath() + "/course-detail?id=" + courseId + "&error=registration_confirmation_failed");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la confirmation: " + e.getMessage());
            e.printStackTrace();

            // Vérifier avant de faire le nettoyage et la redirection
            if (!resp.isCommitted()) {
                try {
                    paiementService.cancelPayment(sessionId);
                    System.out.println("Nettoyage effectué");
                } catch (Exception cleanupException) {
                    System.err.println("Erreur lors du nettoyage: " + cleanupException.getMessage());
                }

                resp.sendRedirect(req.getContextPath() + "/course-detail?id=" + courseId + "&error=internal_error");
            } else {
                System.err.println("Impossible de rediriger après erreur, réponse déjà commitée");
            }
        }
    }

    private String generateUniqueBibNumber() {
        Random random = new Random();
        int number = random.nextInt(9000) + 1000; // Entre 1000 et 9999
        return String.valueOf(number);
    }

    private void handlePaymentCancel(HttpServletRequest req, HttpServletResponse resp,
                                     String courseId, String sessionId)
            throws IOException {

        System.out.println("=== Paiement annulé par l'utilisateur ===");

        // Vérifier que la réponse n'est pas déjà commitée
        if (resp.isCommitted()) {
            System.err.println("Réponse déjà commitée, impossible de rediriger");
            return;
        }

        // Nettoyer l'inscription en attente
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            try {
                paiementService.cancelPayment(sessionId);
                System.out.println("Inscription annulée nettoyée");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'annulation: " + e.getMessage());
            }
        }

        // Rediriger seulement si possible
        if (!resp.isCommitted()) {
            resp.sendRedirect(req.getContextPath() + "/course-detail?id=" + courseId + "&info=payment_cancelled");
        }
    }
}