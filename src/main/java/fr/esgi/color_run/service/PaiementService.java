package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Course_member;

public interface PaiementService {

    /**
     * Crée une session de paiement Stripe et une inscription PENDING
     */
    String createStripeSession(Long courseId, Long memberId, String courseName, Double price);

    /**
     * Confirme le paiement et l'inscription
     */
    Course_member confirmPayment(String stripeSessionId);

    /**
     * Annule le paiement et nettoie l'inscription
     */
    void cancelPayment(String stripeSessionId);

    /**
     * Vérifie le statut d'un paiement Stripe
     */
    boolean isPaymentCompleted(String stripeSessionId);
}