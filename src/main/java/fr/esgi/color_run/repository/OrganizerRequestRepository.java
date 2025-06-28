package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.OrganizerRequest;
import fr.esgi.color_run.business.RequestStatus;
import java.util.List;
import java.util.Optional;

public interface OrganizerRequestRepository {
    OrganizerRequest save(OrganizerRequest request);
    Optional<OrganizerRequest> findById(Long id);
    List<OrganizerRequest> findAll();
    List<OrganizerRequest> findPendingRequests();
    List<OrganizerRequest> findByMemberId(Long memberId);
    List<OrganizerRequest> findByStatus(RequestStatus status);
    OrganizerRequest update(OrganizerRequest request);
    boolean hasActivePendingRequest(Long memberId);
    Boolean deleteById(Long id);
}