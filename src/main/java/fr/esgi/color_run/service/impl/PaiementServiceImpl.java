package fr.esgi.color_run.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Status;
import fr.esgi.color_run.repository.impl.Course_memberRepositoryImpl;
import fr.esgi.color_run.service.Course_memberService;
import fr.esgi.color_run.service.PaiementService;
import fr.esgi.color_run.util.Config;

import java.time.LocalDateTime;
import java.util.Optional;

public class PaiementServiceImpl implements PaiementService {

    private final Course_memberService courseMemberService;
    private final Course_memberRepositoryImpl courseMemberRepository; // Repository direct pour Stripe

    public PaiementServiceImpl(Course_memberService courseMemberService) {
        this.courseMemberService = courseMemberService;
        this.courseMemberRepository = new Course_memberRepositoryImpl(); // Instance directe

        // Configurer Stripe
        String secretKey = Config.get("stripe.secret.key");
        if (secretKey != null && !secretKey.trim().isEmpty()) {
            Stripe.apiKey = secretKey;
            System.out.println("Stripe configuré");
        } else {
            System.err.println("Clé Stripe manquante dans config.properties");
        }
    }

    @Override
    public String createStripeSession(Long courseId, Long memberId, String courseName, Double price) {
        System.out.println("=== Création session Stripe ===");
        System.out.println("Course: " + courseName + " (" + courseId + ")");
        System.out.println("Member: " + memberId + ", Prix: " + price + "€");

        try {
            // Convertir le prix en centimes
            Long priceInCents = Math.round(price * 100);

            // URLs de callback
            String successUrl = Config.get("stripe.success.url") + "?courseId=" + courseId;
            String cancelUrl = Config.get("stripe.cancel.url") + "?courseId=" + courseId;

            // Créer la session Stripe
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
                    .putMetadata("courseId", courseId.toString())
                    .putMetadata("memberId", memberId.toString())
                    .build();

            Session session = Session.create(params);
            System.out.println("Session Stripe créée: " + session.getId());

            // Créer l'inscription PENDING via repository Stripe
            Course_member pendingRegistration = new Course_member();
            pendingRegistration.setCourseId(courseId);
            pendingRegistration.setMemberId(memberId);
            pendingRegistration.setRegistrationStatus(Status.PENDING);
            pendingRegistration.setRegistrationDate(LocalDateTime.now().toString());
            pendingRegistration.setStripeSessionId(session.getId());

            courseMemberRepository.saveWithStripe(pendingRegistration);
            System.out.println("Inscription PENDING créée avec session ID: " + session.getId());

            return session.getId();

        } catch (StripeException e) {
            System.err.println("Erreur Stripe: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de la session de paiement", e);
        }
    }

    @Override
    public Course_member confirmPayment(String stripeSessionId) {
        System.out.println("=== Confirmation paiement ===");
        System.out.println("Session: " + stripeSessionId);

        try {
            // Vérifier le statut de la session Stripe
            Session session = Session.retrieve(stripeSessionId);

            System.out.println("Status Stripe: " + session.getStatus());
            System.out.println("Payment Status: " + session.getPaymentStatus());

            if (!"complete".equals(session.getStatus()) || !"paid".equals(session.getPaymentStatus())) {
                System.err.println("Paiement non confirmé - Status: " + session.getStatus() + ", Payment: " + session.getPaymentStatus());
                return null;
            }

            // Chercher l'inscription PENDING via repository Stripe
            Optional<Course_member> registrationOpt = courseMemberRepository.findByStripeSessionId(stripeSessionId);

            if (registrationOpt.isPresent()) {
                Course_member registration = registrationOpt.get();
                registration.setRegistrationStatus(Status.ACCEPTED);

                // Mettre à jour via repository Stripe
                courseMemberRepository.saveWithStripe(registration);

                System.out.println("Paiement confirmé et inscription validée");
                return registration;
            } else {
                System.err.println("Inscription PENDING introuvable pour: " + stripeSessionId);
                return null;
            }

        } catch (StripeException e) {
            System.err.println("Erreur Stripe lors de la confirmation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void cancelPayment(String stripeSessionId) {
        System.out.println("=== Annulation paiement ===");
        System.out.println("Session: " + stripeSessionId);

        // Chercher via repository Stripe
        Optional<Course_member> registrationOpt = courseMemberRepository.findByStripeSessionId(stripeSessionId);

        if (registrationOpt.isPresent()) {
            // Supprimer via service standard
            courseMemberService.delete(registrationOpt.get());
            System.out.println("✅ Inscription PENDING supprimée");
        } else {
            System.out.println("ℹ️ Aucune inscription PENDING à supprimer");
        }
    }

    @Override
    public boolean isPaymentCompleted(String stripeSessionId) {
        try {
            Session session = Session.retrieve(stripeSessionId);
            return "complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus());
        } catch (StripeException e) {
            System.err.println("Erreur lors de la vérification du paiement: " + e.getMessage());
            return false;
        }
    }
}