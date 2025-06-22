// MessageRepository.java
package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Message;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {

    /**
     * Sauvegarde un nouveau message
     */
    Message save(Message message);

    /**
     * Trouve tous les messages d'une discussion, ordonnés par date
     */
    List<Message> findByDiscussionId(Long discussionId);

    /**
     * Trouve les messages d'une discussion depuis un ID donné
     */
    List<Message> findByDiscussionIdSinceId(Long discussionId, Long sinceId);

    /**
     * Trouve un message par son ID
     */
    Optional<Message> findById(Long id);

    /**
     * Met à jour un message
     */
    Message update(Message message);

    /**
     * Masque un message
     */
    void hideMessage(Long messageId);

    /**
     * Épingle ou désépingle un message
     */
    void togglePin(Long messageId);

    /**
     * Supprime un message
     */
    void delete(Long messageId);

    /**
     * Compte le nombre de messages d'une discussion
     */
    int countByDiscussionId(Long discussionId);

    /**
     * Trouve les derniers messages d'une discussion
     */
    List<Message> findRecentByDiscussionId(Long discussionId, int limit);
}