package fr.esgi.color_run.service;

import fr.esgi.color_run.business.OrganizerRequest;
import fr.esgi.color_run.business.RequestStatus;
import java.util.List;
import java.util.Optional;

public interface OrganizerRequestService {
    OrganizerRequest submitRequest(Long memberId, String motivation, Long existingAssociationId);
    List<OrganizerRequest> getAllRequests();
    List<OrganizerRequest> getPendingRequests();
    List<OrganizerRequest> getRequestsByMember(Long memberId);
    Optional<OrganizerRequest> getRequest(Long id);
    OrganizerRequest approveRequest(Long requestId, Long adminId, String comment);
    OrganizerRequest rejectRequest(Long requestId, Long adminId, String comment);
    boolean hasActivePendingRequest(Long memberId);
    boolean canMemberSubmitRequest(Long memberId);
}