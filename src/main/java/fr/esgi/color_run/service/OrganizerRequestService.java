package fr.esgi.color_run.service;

import fr.esgi.color_run.business.OrganizerRequest;
import fr.esgi.color_run.business.RequestType;
import java.util.List;
import java.util.Optional;

public interface OrganizerRequestService {


    // Soumettre une demande standard (devenir organisateur ou rejoindre association existante)
    void submitRequest(Long memberId, RequestType requestType, String motivation, Long existingAssociationId) throws Exception;

    // Soumettre une demande avec création d'une nouvelle association
    void submitRequestWithNewAssociation(Long memberId, RequestType requestType, String motivation,
                                         String assocName, String assocEmail, String assocDescription,
                                         String assocWebsiteLink, String assocPhone, String assocAddress,
                                         String assocZipCode, String assocCity) throws Exception;

    // Vérifier si un membre peut soumettre une nouvelle demande
    boolean canMemberSubmitRequest(Long memberId) throws Exception;

    // Vérifier si un membre a une demande en attente
    boolean hasActivePendingRequest(Long memberId) throws Exception;

    // Récupérer toutes les demandes en attente
    List<OrganizerRequest> getPendingRequests() throws Exception;

    // Récupérer toutes les demandes (tous statuts)
    List<OrganizerRequest> getAllRequests() throws Exception;

    // Récupérer les demandes d'un membre spécifique
    List<OrganizerRequest> getRequestsByMember(Long memberId) throws Exception;

    // Approuver une demande
    OrganizerRequest approveRequest(Long requestId, Long adminId, String comment) throws Exception;

    // Refuser une demande
    OrganizerRequest rejectRequest(Long requestId, Long adminId, String comment) throws Exception;

    // Récupérer une demande par son ID
    Optional<OrganizerRequest> getRequestById(Long requestId) throws Exception;

    // Supprimer une demande (utilisé pour le nettoyage)
    void deleteRequest(Long requestId) throws Exception;
}