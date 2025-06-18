package fr.esgi.color_run.servlet;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Status;
import fr.esgi.color_run.repository.impl.Course_memberRepositoryImpl;
import fr.esgi.color_run.util.Config;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

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

        // Récupérer les paramètres
        String courseId = req.getParameter("courseId");
        String courseName = req.getParameter("courseName");
        String priceStr = req.getParameter("price");

        try {
            // VÉRIFICATION IMMÉDIATE : L'utilisateur est-il déjà inscrit ?
            Course_memberRepositoryImpl courseMemberRepo = new Course_memberRepositoryImpl();

            if (courseMemberRepo.isMemberRegisteredAndPaid(Long.parseLong(courseId), member.getId())) {
                System.err.println("Membre déjà inscrit et payé pour cette course");
                resp.setStatus(400);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Vous êtes déjà inscrit à cette course\"}");
                return;
            }

            if (courseMemberRepo.isMemberInCourse(Long.parseLong(courseId), member.getId())) {
                System.err.println("Inscription en attente existe déjà");
                resp.setStatus(400);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Une inscription est déjà en cours de traitement pour cette course\"}");
                return;
            }

            // Continuer avec la création de la session Stripe seulement si pas d'inscription existante
            Double price = Double.parseDouble(priceStr);
            Long priceInCents = Math.round(price * 100); // Stripe utilise les centimes

            System.out.println("Prix converti: " + priceInCents + " centimes");

            // Configurer Stripe avec la clé secrète
            String secretKey = Config.get("stripe.secret.key");
            if (secretKey == null || secretKey.trim().isEmpty()) {
                System.err.println("Clé secrète Stripe manquante dans config.properties");
                resp.setStatus(500);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Configuration Stripe manquante\"}");
                return;
            }

            Stripe.apiKey = secretKey;
            System.out.println("Stripe configuré avec la clé: " + secretKey.substring(0, 12) + "...");

            // URLs de succès et d'annulation
            String successUrl = Config.get("stripe.success.url") + "?courseId=" + courseId + "&session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = Config.get("stripe.cancel.url") + "?courseId=" + courseId + "&session_id={CHECKOUT_SESSION_ID}";

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
            System.out.println("Session créée avec l'ID: " + session.getId());

            // Enregistrer l'inscription PENDING dans la base de données
            try {
                Course_member pendingRegistration = new Course_member();
                pendingRegistration.setCourseId(Long.parseLong(courseId));
                pendingRegistration.setMemberId(member.getId());
                pendingRegistration.setRegistrationStatus(Status.PENDING);
                pendingRegistration.setRegistrationDate(LocalDateTime.now().toString());
                pendingRegistration.setStripeSessionId(session.getId());

                courseMemberRepo.saveWithStripe(pendingRegistration);
                System.out.println("Inscription PENDING créée avec session ID: " + session.getId());

            } catch (Exception e) {
                System.err.println("Erreur lors de la création de l'inscription PENDING: " + e.getMessage());
                e.printStackTrace();
                // Continuer quand même pour ne pas bloquer le processus Stripe
            }

            // Retourner l'ID de session à JavaScript
            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":\"" + session.getId() + "\"}");

            System.out.println("Réponse JSON envoyée au client");

        } catch (NumberFormatException e) {
            System.err.println("Erreur de conversion du prix: " + e.getMessage());
            resp.setStatus(400);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Prix invalide\"}");
        } catch (StripeException e) {
            System.err.println("Erreur Stripe: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(400);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Erreur lors de la création de la session de paiement: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
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