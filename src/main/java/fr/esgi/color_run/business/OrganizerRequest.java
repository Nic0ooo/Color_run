
package fr.esgi.color_run.business;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class OrganizerRequest {
    private Long id;
    private Long memberId;
    private RequestType requestType;
    private String motivation;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    private Long processedByAdminId; // Admin ID who processed the request
    private String adminComment;
    private String memberRoleName;

    // Association-related fields
    private Long existingAssociationId;
    private String existingAssociationName; // For display purposes

    // New association fields (if creating new association)
    private String newAssociationName;
    private String newAssociationEmail;
    private String newAssociationDescription;
    private String newAssociationWebsiteLink;
    private String newAssociationPhone;
    private String newAssociationAddress;
    private String newAssociationZipCode;
    private String newAssociationCity;

    // Constructors
    public OrganizerRequest() {
        this.status = RequestStatus.PENDING;
        this.requestDate = LocalDateTime.now();
        this.requestType = RequestType.BECOME_ORGANIZER;
    }

    public OrganizerRequest(Long memberId, RequestType requestType, String motivation) {
        this();
        this.memberId = memberId;
        this.requestType = requestType;
        this.motivation = motivation;
    }

    // Utility methods for Thymeleaf
    public String getFormattedRequestDate() {
        if (requestDate == null) return "";
        return requestDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }

    public String getFormattedProcessedDate() {
        if (processedDate == null) return "";
        return processedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }

    public boolean hasNewAssociation() {
        return newAssociationName != null && !newAssociationName.trim().isEmpty();
    }

    public boolean hasExistingAssociation() {
        return existingAssociationId != null;
    }

    @Override
    public String toString() {
        return "OrganizerRequest{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", requestType=" + requestType +
                ", status=" + status +
                ", existingAssociationId=" + existingAssociationId +
                ", newAssociationName='" + newAssociationName + '\'' +
                '}';
    }
}