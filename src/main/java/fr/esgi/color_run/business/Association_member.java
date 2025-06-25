package fr.esgi.color_run.business;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente la liaison entre un organisateur et une association
 * Un organisateur ne peut appartenir qu'à une seule association
 */
@Data
public class Association_member {
    private Long id;
    private Long memberId; // Forcément un organisateur
    private Long associationId;
    private String memberName;
    private String memberEmail;
    private String associationName;
    private LocalDateTime joinDate;

    private static Long compteur = 0L;

    public Association_member(Long memberId, Long associationId) {
        this.id = compteur++;
        this.memberId = memberId;
        this.associationId = associationId;
        this.joinDate = LocalDateTime.now();
    }

    public String getFormattedJoinDate() {
        if (joinDate == null) return "";
        return joinDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}