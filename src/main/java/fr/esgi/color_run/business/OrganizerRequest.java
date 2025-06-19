package fr.esgi.color_run.business;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class OrganizerRequest {
    private Long id;
    private Long memberId;
    private String motivation;
    private Long existingAssociationId; // Association existante (optionnel)
    private Association newAssociation; // Nouvelle association demandée (optionnel)
    private LocalDateTime requestDate;
    private RequestStatus status; // PENDING, APPROVED, REJECTED
    private String adminComment;
    private Long processedByAdminId;
    private LocalDateTime processedDate;

    private static Long compteur = 0L;

    public OrganizerRequest() {
        this.id = compteur++;
        this.requestDate = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    public String getFormattedRequestDate() {
        if (requestDate == null) return "";
        return requestDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }

    public String getFormattedProcessedDate() {
        if (processedDate == null) return "";
        return processedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }
}