package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.*;
import fr.esgi.color_run.repository.MemberRepository;
import fr.esgi.color_run.repository.OrganizerRequestRepository;
import fr.esgi.color_run.repository.impl.MemberRepositoryImpl;
import fr.esgi.color_run.repository.impl.OrganizerRequestRepositoryImpl;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.OrganizerRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrganizerRequestServiceImpl implements OrganizerRequestService {

    private final OrganizerRequestRepository organizerRequestRepository = new OrganizerRequestRepositoryImpl();
    private final MemberRepository memberRepository = new MemberRepositoryImpl();

    private final MemberService memberService = new MemberServiceImpl();

    @Override
    public void submitRequest(Long memberId, RequestType requestType, String motivation, Long existingAssociationId) {
        System.out.println("📝 OrganizerRequestService.submitRequest() appelé");
        System.out.println("  - Member ID: " + memberId);
        System.out.println("  - Motivation length: " + (motivation != null ? motivation.length() : "null"));
        System.out.println("  - Association ID: " + existingAssociationId);

        // Vérifications
        if (!canMemberSubmitRequest(memberId)) {
            System.out.println("❌ Le membre a déjà une demande en cours ou ne peut pas soumettre de demande");
            throw new IllegalStateException("Le membre a déjà une demande en cours ou ne peut pas soumettre de demande");
        }

        // Valider la motivation
        if (motivation == null || motivation.trim().length() < 50) {
            throw new IllegalArgumentException("La motivation doit contenir au moins 50 caractères");
        }

        try {
            OrganizerRequest request = new OrganizerRequest();
            request.setMemberId(memberId);
            request.setRequestType(requestType);
            request.setMotivation(motivation);
            request.setExistingAssociationId(existingAssociationId);
            request.setStatus(RequestStatus.PENDING);
            request.setRequestDate(LocalDateTime.now());

            System.out.println("💾 Sauvegarde de la demande...");
            organizerRequestRepository.save(request);

            System.out.println("✅ Service - Demande créée avec ID: " + request.getId());

        } catch (Exception e) {
            System.err.println("❌ Erreur dans submitRequest:");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void submitRequestWithNewAssociation(Long memberId, RequestType requestType, String motivation, String assocName, String assocEmail, String assocDescription, String assocWebsiteLink, String assocPhone, String assocAddress, String assocZipCode, String assocCity) {
        System.out.println("🔍 Service - submitRequestWithNewAssociation appelé pour membre " + memberId);

        try {
            // Vérifications
            if (!canMemberSubmitRequest(memberId)) {
                System.out.println("❌ Le membre a déjà une demande en cours ou ne peut pas soumettre de demande");
                throw new IllegalStateException("Le membre a déjà une demande en cours ou ne peut pas soumettre de demande");
            }

            // Valider les données
            if (motivation == null || motivation.trim().length() < 50) {
                throw new IllegalArgumentException("La motivation doit contenir au moins 50 caractères");
            }

            if (assocName == null || assocName.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de l'association est obligatoire");
            }

            if (assocEmail == null || assocEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("L'email de l'association est obligatoire");
            }

            if (assocDescription == null || assocDescription.trim().isEmpty()) {
                throw new IllegalArgumentException("La description de l'association est obligatoire");
            }

            OrganizerRequest request = new OrganizerRequest();
            request.setMemberId(memberId);
            request.setRequestType(requestType);
            request.setMotivation(motivation.trim());
            request.setStatus(RequestStatus.PENDING);
            request.setRequestDate(LocalDateTime.now());

            request.setNewAssociationName(assocName.trim());
            request.setNewAssociationEmail(assocEmail.trim().toLowerCase());
            request.setNewAssociationDescription(assocDescription.trim());
            request.setNewAssociationWebsiteLink(assocWebsiteLink != null ? assocWebsiteLink.trim() : null);
            request.setNewAssociationPhone(assocPhone != null ? assocPhone.trim() : null);
            request.setNewAssociationAddress(assocAddress != null ? assocAddress.trim() : null);
            request.setNewAssociationZipCode(assocZipCode != null ? assocZipCode.trim() : null);
            request.setNewAssociationCity(assocCity != null ? assocCity.trim() : null);

            System.out.println("📝 Tentative de sauvegarde de la demande...");
            System.out.println("  - Membre ID: " + memberId);
            System.out.println("  - Type: " + requestType);
            System.out.println("  - Nom association: " + assocName);
            System.out.println("  - Email association: " + assocEmail);

            // Sauvegarder la demande
            OrganizerRequest savedRequest = organizerRequestRepository.save(request);

            if (savedRequest != null && savedRequest.getId() != null) {
                System.out.println("✅ Service - Demande avec nouvelle association créée avec ID: " + savedRequest.getId());
            } else {
                throw new RuntimeException("Échec de la sauvegarde - aucun ID généré");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans submitRequestWithNewAssociation:");
            System.err.println("  - Type: " + e.getClass().getSimpleName());
            System.err.println("  - Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de la demande avec nouvelle association", e);
        }
    }

    @Override
    public List<OrganizerRequest> getAllRequests() throws Exception {
        System.out.println("🔍 Service - Récupération de toutes les demandes");
        List<OrganizerRequest> requests = organizerRequestRepository.findAll();
        System.out.println("🔍 Service - " + requests.size() + " demandes totales trouvées");
        return requests;
    }

    @Override
    public List<OrganizerRequest> getPendingRequests() {
        System.out.println("🔍 Service - Récupération des demandes en attente");
        List<OrganizerRequest> requests = organizerRequestRepository.findByStatus(RequestStatus.PENDING);
        System.out.println("🔍 Service - " + requests.size() + " demandes en attente trouvées");
        return requests;
    }

    @Override
    public List<OrganizerRequest> getRequestsByMember(Long memberId) {
        return organizerRequestRepository.findByMemberId(memberId);
    }

    @Override
    public OrganizerRequest approveRequest(Long requestId, Long adminId, String comment) {
        System.out.println("🔍 Service - Approbation de la demande " + requestId + " par admin " + adminId);

        Optional<OrganizerRequest> requestOpt = organizerRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Demande introuvable");
        }

        OrganizerRequest request = requestOpt.get();
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Cette demande a déjà été traitée");
        }

        // Mettre à jour le statut de la demande
        request.setStatus(RequestStatus.APPROVED);
        request.setAdminComment(comment);
        request.setProcessedByAdminId(adminId);
        request.setProcessedDate(LocalDateTime.now());

        // Actions spécifiques selon le type de demande
        if (request.getRequestType() == RequestType.BECOME_ORGANIZER) {
            // Promouvoir le membre en organisateur
            Optional<Member> memberOpt = memberService.getMember(request.getMemberId());
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                member.setRole(Role.ORGANIZER);
                memberService.updateMember(member.getId(), member);
            }
        }

        organizerRequestRepository.update(request);

        System.out.println("✅ Service - Demande " + requestId + " approuvée");
        return request;
    }

    @Override
    public OrganizerRequest rejectRequest(Long requestId, Long adminId, String comment) {
        Optional<OrganizerRequest> requestOpt = organizerRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Demande introuvable");
        }

        OrganizerRequest request = requestOpt.get();
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Cette demande a déjà été traitée");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setAdminComment(comment);
        request.setProcessedByAdminId(adminId);
        request.setProcessedDate(LocalDateTime.now());

        // Sauvegarder
        organizerRequestRepository.update(request);

        System.out.println("✅ Service - Demande " + requestId + " rejetée");
        return request;
    }

    @Override
    public boolean canMemberSubmitRequest(Long memberId) {
        System.out.println("🔍 Vérification canMemberSubmitRequest pour membre: " + memberId);

        // Vérifier qu'il n'a pas de demande en cours
        boolean hasActiveRequest = hasActivePendingRequest(memberId);

        System.out.println("🔍 Service - Membre " + memberId + " a une demande active: " + hasActiveRequest);

        return !hasActiveRequest;
    }

    @Override
    public boolean hasActivePendingRequest(Long memberId) {
        return organizerRequestRepository.hasActivePendingRequest(memberId);
    }

    @Override
    public Optional<OrganizerRequest> getRequestById(Long requestId) throws Exception {
        return organizerRequestRepository.findById(requestId);
    }

    @Override
    public void deleteRequest(Long requestId) throws Exception {
        System.out.println("🔍 Service - Suppression de la demande " + requestId);
        organizerRequestRepository.deleteById(requestId);
        System.out.println("✅ Service - Demande " + requestId + " supprimée");
    }

}