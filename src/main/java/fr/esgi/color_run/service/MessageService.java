package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Message;
import java.util.List;

public interface MessageService {

    /**
     * Envoie un nouveau message dans une course
     */
    Message sendMessage(Long courseId, Long memberId, String content);

    /**
     * ✅ Nouveau : Modifie le contenu d'un message (seul l'auteur peut modifier)
     */
    Message updateMessage(Long messageId, Long requesterId, String newContent);

    /**
     * ✅ Nouveau : Supprime son propre message (marque comme supprimé par l'auteur)
     */
    boolean deleteOwnMessage(Long messageId, Long requesterId);

    /**
     * Récupère tous les messages d'une course
     */
    List<Message> getCourseMessages(Long courseId, Long memberId);

    /**
     * Récupère les nouveaux messages depuis un ID donné
     */
    List<Message> getNewMessages(Long courseId, Long memberId, Long sinceMessageId);

    /**
     * Vérifie si un membre peut accéder au chat d'une course
     */
    boolean canAccessChat(Long courseId, Long memberId);

    /**
     * Épingle ou désépingle un message (modérateurs uniquement)
     */
    boolean togglePinMessage(Long messageId, Long moderatorId);

    /**
     * ✅ Mis à jour : Masque un message pour raison de modération (modérateurs uniquement)
     * Différent de deleteOwnMessage - ici le contenu est préservé mais le message est marqué comme masqué
     */
    boolean hideMessage(Long messageId, Long moderatorId);

    /**
     * ✅ Mis à jour : Supprime définitivement un message (modérateurs uniquement)
     * Suppression complète de la base de données
     */
    boolean deleteMessage(Long messageId, Long moderatorId);

    /**
     * Vérifie si un utilisateur est modérateur d'une course
     */
    boolean isModerator(Long courseId, Long memberId);

    /**
     * Crée un message de bienvenue si nécessaire pour un nouveau membre dans une course
     */
    void createWelcomeMessageIfNeeded(Long courseId, Long memberId);
}