package fr.esgi.color_run.servlet;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.util.Config;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/create-checkout-session")
public class CreateCheckoutSessionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        System.out.println("=== CreateCheckoutSessionServlet appelé ===");
        
        // Vérifier que l'utilisateur est connecté
        Member member = (Member) req.getSession().getAttribute("member");
        if (member == null) {
            resp.setStatus(401);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Vous devez être connecté pour vous inscrire\"}");
            return;
        }
        
        // Configurer Stripe avec la clé secrète
        String secretKey = Config.get("stripe.secret.key");
        if (secretKey == null || secretKey.trim().isEmpty()) {
            System.err.println("❌ Clé secrète Stripe manquante dans config.properties");
            resp.setStatus(500);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Configuration Stripe manquante\"}");
            return;
        }
        
        Stripe.apiKey = secretKey;
        System.out.println("✅ Stripe configuré avec la clé: " + secretKey.substring(0, 12) + "...");
        
        // Récupérer les paramètres
        String courseId = req.getParameter("courseId");
        String courseName = req.getParameter("courseName");
        String priceStr = req.getParameter("price");
        
        System.out.println("Paramètres reçus:");
        System.out.println("  - courseId: " + courseId);
        System.out.println("  - courseName: " + courseName);
        System.out.println("  - price: " + priceStr);
        System.out.println("  - memberId: " + member.getId());
        
        try {
            Double price = Double.parseDouble(priceStr);
            Long priceInCents = Math.round(price * 100); // Stripe utilise les centimes
            
            System.out.println("Prix converti: " + priceInCents + " centimes");
            
            // URLs de succès et d'annulation
            String successUrl = Config.get("stripe.success.url") + "?courseId=" + courseId;
            String cancelUrl = Config.get("stripe.cancel.url") + "?courseId=" + courseId;
            
            System.out.println("URLs configurées:");
            System.out.println("  - Success: " + successUrl);
            System.out.println("  - Cancel: " + cancelUrl);
            
            // Créer session Stripe Checkout
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(priceInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Inscription - " + courseName)
                                        .setDescription("Inscription à la course " + courseName)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .putMetadata("courseId", courseId)
                .putMetadata("memberId", member.getId().toString())
                .build();
            
            System.out.println("Création de la session Stripe...");
            Session session = Session.create(params);
            System.out.println("✅ Session créée avec l'ID: " + session.getId());
            
            // TODO: Enregistrer l'inscription en statut PENDING dans la base de données
            // savePendingRegistration(courseId, member.getId(), session.getId());
            
            // Retourner l'ID de session à JavaScript
            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":\"" + session.getId() + "\"}");
            
            System.out.println("✅ Réponse JSON envoyée au client");
            
        } catch (NumberFormatException e) {
            System.err.println("❌ Erreur de conversion du prix: " + e.getMessage());
            resp.setStatus(400);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Prix invalide\"}");
        } catch (StripeException e) {
            System.err.println("❌ Erreur Stripe: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(400);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Erreur lors de la création de la session de paiement: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Erreur interne du serveur\"}");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setStatus(405);
        resp.getWriter().write("Méthode GET non supportée");
    }
}
