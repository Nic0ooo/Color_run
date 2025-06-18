package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Course_member;

public interface PaiementService {

    String createStripeSession(Long courseId, Long memberId, String courseName, Double price);

    Course_member confirmPayment(String stripeSessionId);

    void cancelPayment(String stripeSessionId);
    
    boolean isPaymentCompleted(String stripeSessionId);
}