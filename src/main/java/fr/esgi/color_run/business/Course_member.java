package fr.esgi.color_run.business;

import lombok.Data;

@Data
public class Course_member {
    private Long id;
    private Long courseId;
    private Long memberId;
    private String registrationDate;
    private Status registrationStatus;
    private String stripeSessionId;
    private String bibNumber; // ✅ Nouveau champ pour le numéro de dossard

    private static Long compteur = 0L;

    public Course_member() {
        this.id = compteur++;
        this.registrationStatus = Status.PENDING;
    }

    /**
     * Vérifie si un dossard a été généré
     */
    public boolean hasBibNumber() {
        return bibNumber != null && !bibNumber.trim().isEmpty();
    }

    /**
     * Vérifie si le membre peut générer un dossard
     */
    public boolean canGenerateBib() {
        return registrationStatus == Status.ACCEPTED && !hasBibNumber();
    }
}