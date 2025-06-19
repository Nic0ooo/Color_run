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
    private LocalDateTime joinDate;

    private static Long compteur = 0L;

    public Association_member() {
        this.id = compteur++;
        this.joinDate = LocalDateTime.now();
    }

    public String getFormattedJoinDate() {
        if (joinDate == null) return "";
        return joinDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}