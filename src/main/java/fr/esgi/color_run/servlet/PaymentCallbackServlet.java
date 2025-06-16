package fr.esgi.color_run.servlet;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import fr.esgi.color_run.util.Config;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/payment-success", "/payment-cancel"})
public class PaymentCallbackServlet extends HttpServlet {

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
        
        if ("/payment-success".equals(servletPath)) {
            handlePaymentSuccess(req, resp, courseId, sessionId);
        } else if ("/payment-cancel".equals(servletPath)) {
            handlePaymentCancel(req, resp, courseId);
        } else {
            resp.sendError(404, "Page non trouvée");
        }
    }
    
    private void handlePaymentSuccess(HttpServletRequest req, HttpServletResponse resp, 
                                    String courseId, String sessionId) 
            throws IOException {
        
        System.out.println("=== Traitement du paiement réussi ===");
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            System.err.println("❌ Session ID manquant");
            resp.sendRedirect("/color_run_war/course-detail?id=" + courseId + "&error=missing_session");
            return;
        }
        
        try {
            // Configurer Stripe
            Stripe.apiKey = Config.get("stripe.secret.key");
            
            // Vérifier le statut de la session Stripe
            Session session = Session.retrieve(sessionId);
            System.out.println("Session Stripe récupérée:");
            System.out.println("  - ID: " + session.getId());
            System.out.println("  - Status: " + session.getStatus());
            System.out.println("  - Payment status: " + session.getPaymentStatus());
            
            if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                // Récupérer les métadonnées
                String courseIdFromStripe = session.getMetadata().get("courseId");
                String memberIdFromStripe = session.getMetadata().get("memberId");
                
                System.out.println("Métadonnées Stripe:");
                System.out.println("  - courseId: " + courseIdFromStripe);
                System.out.println("  - memberId: " + memberIdFromStripe);
                
                // TODO: Confirmer l'inscription en base de données
                // confirmRegistration(sessionId, courseIdFromStripe, memberIdFromStripe);
                
                System.out.println("✅ Paiement confirmé, redirection vers page de succès");
                resp.sendRedirect("/color_run_war/course-detail?id=" + courseId + "&success=payment_completed");
            } else {
                System.err.println("❌ Paiement non confirmé - Status: " + session.getStatus() + ", Payment: " + session.getPaymentStatus());
                resp.sendRedirect("/color_run_war/course-detail?id=" + courseId + "&error=payment_not_completed");
            }
            
        } catch (StripeException e) {
            System.err.println("❌ Erreur Stripe lors de la vérification: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect("/color_run_war/course-detail?id=" + courseId + "&error=stripe_verification_error");
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la vérification: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect("/color_run_war/course-detail?id=" + courseId + "&error=internal_error");
        }
    }
    
    private void handlePaymentCancel(HttpServletRequest req, HttpServletResponse resp, String courseId) 
            throws IOException {
        
        System.out.println("=== Paiement annulé par l'utilisateur ===");
        
        // TODO: Nettoyer les enregistrements temporaires si nécessaire
        // cleanupPendingRegistration(sessionId);
        
        resp.sendRedirect("/color_run_war/course-detail?id=" + courseId + "&info=payment_cancelled");
    }
}
