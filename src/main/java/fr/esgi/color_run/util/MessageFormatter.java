package fr.esgi.color_run.util;

import fr.esgi.color_run.business.Message;

/**
 * Utilitaire pour formater les messages et leurs données associées
 */
public class MessageFormatter {

    /**
     * Formate le nom complet d'un utilisateur
     */
    public static String formatMemberName(String firstname, String lastname) {
        if (firstname != null && lastname != null) {
            return firstname.trim() + " " + lastname.trim();
        } else if (firstname != null) {
            return firstname.trim();
        } else if (lastname != null) {
            return lastname.trim();
        }
        return "Utilisateur";
    }

    /**
     * Formate le nom d'un membre à partir d'un message
     */
    public static String formatMemberName(Message message) {
        if (message == null) {
            return "Utilisateur inconnu";
        }

        String formatted = formatMemberName(message.getMemberFirstname(), message.getMemberName());

        // Fallback si pas de nom disponible
        if ("Utilisateur".equals(formatted) && message.getMemberId() != null) {
            return "Utilisateur #" + message.getMemberId();
        }

        return formatted;
    }
}