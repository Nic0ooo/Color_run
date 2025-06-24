package fr.esgi.color_run.business;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Message {
    private Long id;
    private Long discussionId;
    private Long memberId;
    private String content;
    private String originalContent;
    private LocalDateTime date;
    private LocalDateTime lastModifiedDate;
    private boolean isPin;
    private boolean isHidden;
    private boolean isModified = false;
    private boolean isDeleted = false;
    private Long hiddenByMemberId;

    private String memberFirstname;
    private String memberName;

    private static Long compteur = 0L;

    public Message() {
        this.id = compteur++;
        this.date = LocalDateTime.now();
        this.isPin = false;
        this.isHidden = false;
        this.isModified = false;
        this.isDeleted = false;
    }

    public Message(Long discussionId, Long memberId, String content) {
        this();
        this.discussionId = discussionId;
        this.memberId = memberId;
        this.content = content;
    }

    /**
     * Retourne la date formatée pour l'affichage
     */
    public String getFormattedDate() {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        return date.format(formatter);
    }

    /**
     * Retourne la date de modification formatée pour l'affichage
     */
    public String getFormattedLastModifiedDate() {
        if (lastModifiedDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        return lastModifiedDate.format(formatter);
    }

    /**
     * Retourne la date sous forme de timestamp ISO pour JavaScript
     */
    public String getIsoDate() {
        if (date == null) return "";
        return date.toString();
    }

    /**
     * Vérifie si le message est valide
     */
    public boolean isValid() {
        return discussionId != null && memberId != null &&
                content != null && !content.trim().isEmpty() &&
                content.length() <= 1000;
    }

    /**
     * Nettoie le contenu du message
     */
    public void sanitizeContent() {
        if (content != null) {
            content = content.trim();
            // Supprimer les caractères de contrôle dangereux
            content = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        }
    }

    /**
     * Sauvegarde le contenu original si ce n'est pas déjà fait
     */
    public void preserveOriginalContent() {
        if (this.originalContent == null && this.content != null) {
            this.originalContent = this.content;
        }
    }

    /**
     * Marque le message comme modifié
     */
    public void markAsModified() {
        this.isModified = true;
        this.lastModifiedDate = LocalDateTime.now();
    }

    /**
     * Marque le message comme supprimé par l'auteur
     */
    public void markAsDeletedByAuthor() {
        this.preserveOriginalContent();
        this.isDeleted = true;
        this.content = "Message supprimé par son auteur";
    }

    /**
     * Marque le message comme masqué par un modérateur
     */
    public void markAsHiddenByModerator(Long moderatorId) {
        this.preserveOriginalContent();
        this.isHidden = true;
        this.hiddenByMemberId = moderatorId;
        this.content = "Message masqué pour raison de modération";
    }

    /**
     * Vérifie si le message peut être modifié
     */
    public boolean canBeEdited() {
        return !isDeleted && !isHidden;
    }

    /**
     * Vérifie si le message peut être supprimé
     */
    public boolean canBeDeleted() {
        return !isDeleted;
    }

    /**
     * Retourne le nom complet du membre
     */
    public String getFullMemberName() {
        if (memberFirstname != null && memberName != null) {
            return memberFirstname.trim() + " " + memberName.trim();
        } else if (memberFirstname != null) {
            return memberFirstname.trim();
        } else if (memberName != null) {
            return memberName.trim();
        }
        return "Utilisateur #" + memberId;
    }
}