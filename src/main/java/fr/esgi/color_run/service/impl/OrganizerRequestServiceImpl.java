package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.OrganizerRequest;
import fr.esgi.color_run.business.RequestStatus;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.repository.OrganizerRequestRepository;
import fr.esgi.color_run.repository.impl.OrganizerRequestRepositoryImpl;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.OrganizerRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrganizerRequestServiceImpl implements OrganizerRequestService {

    private final OrganizerRequestRepository organizerRequestRepository = new OrganizerRequestRepositoryImpl();
    private final MemberService memberService = new MemberServiceImpl();

    @Override
    public OrganizerRequest submitRequest(Long memberId, String motivation, Long existingAssociationId) {
        System.out.println("📝 OrganizerRequestService.submitRequest() appelé");
        System.out.println("  - Member ID: " + memberId);
        System.out.println("  - Motivation length: " + (motivation != null ? motivation.length() : "null"));
        System.out.println("  - Association ID: " + existingAssociationId);

        // Vérifications
        if (!canMemberSubmitRequest(memberId)) {
            System.out.println("❌ Le membre ne peut pas soumettre de demande");
            throw new IllegalStateException("Le membre ne peut pas soumettre de demande");
        }

        try {
            OrganizerRequest request = new OrganizerRequest();
            request.setMemberId(memberId);
            request.setMotivation(motivation);
            request.setExistingAssociationId(existingAssociationId);
            request.setRequestDate(LocalDateTime.now());
            request.setStatus(RequestStatus.PENDING);

            System.out.println("💾 Sauvegarde de la demande...");
            OrganizerRequest savedRequest = organizerRequestRepository.save(request);

            if (savedRequest != null) {
                System.out.println("✅ Demande sauvegardée avec ID: " + savedRequest.getId());
            } else {
                System.out.println("❌ Échec de la sauvegarde");
            }

            return savedRequest;

        } catch (Exception e) {
            System.err.println("❌ Erreur dans submitRequest:");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<OrganizerRequest> getAllRequests() {
        return organizerRequestRepository.findAll();
    }

    @Override
    public List<OrganizerRequest> getPendingRequests() {
        return organizerRequestRepository.findPendingRequests();
    }

    @Override
    public List<OrganizerRequest> getRequestsByMember(Long memberId) {
        return organizerRequestRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<OrganizerRequest> getRequest(Long id) {
        return organizerRequestRepository.findById(id);
    }

    @Override
    public OrganizerRequest approveRequest(Long requestId, Long adminId, String comment) {
        Optional<OrganizerRequest> requestOpt = organizerRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Demande introuvable");
        }

        OrganizerRequest request = requestOpt.get();
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("La demande a déjà été traitée");
        }

        // Mettre à jour le statut de la demande
        request.setStatus(RequestStatus.APPROVED);
        request.setAdminComment(comment);
        request.setProcessedByAdminId(adminId);
        request.setProcessedDate(LocalDateTime.now());

        // Mettre à jour le rôle du membre
        Optional<Member> memberOpt = memberService.getMember(request.getMemberId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            member.setRole(Role.ORGANIZER);
            memberService.updateMember(member.getId(), member);
        }

        return organizerRequestRepository.update(request);
    }

    @Override
    public OrganizerRequest rejectRequest(Long requestId, Long adminId, String comment) {
        Optional<OrganizerRequest> requestOpt = organizerRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Demande introuvable");
        }

        OrganizerRequest request = requestOpt.get();
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("La demande a déjà été traitée");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setAdminComment(comment);
        request.setProcessedByAdminId(adminId);
        request.setProcessedDate(LocalDateTime.now());

        return organizerRequestRepository.update(request);
    }

    @Override
    public boolean hasActivePendingRequest(Long memberId) {
        return organizerRequestRepository.hasActivePendingRequest(memberId);
    }

    @Override
    public boolean canMemberSubmitRequest(Long memberId) {
        System.out.println("🔍 Vérification canMemberSubmitRequest pour membre: " + memberId);

        try {
            // Vérifier que le membre existe et n'est pas déjà organisateur
            Optional<Member> memberOpt = memberService.getMember(memberId);
            if (memberOpt.isEmpty()) {
                System.out.println("❌ Membre introuvable");
                return false;
            }

            Member member = memberOpt.get();
            System.out.println("✅ Membre trouvé: " + member.getEmail() + " (rôle: " + member.getRole() + ")");

            if (member.getRole() == Role.ORGANIZER || member.getRole() == Role.ADMIN) {
                System.out.println("❌ Membre déjà organisateur ou admin");
                return false;
            }

            // Vérifier qu'il n'a pas déjà une demande en cours
            boolean hasPending = hasActivePendingRequest(memberId);
            System.out.println("🔍 A une demande en cours: " + hasPending);

            return !hasPending;

        } catch (Exception e) {
            System.err.println("❌ Erreur dans canMemberSubmitRequest:");
            e.printStackTrace();
            return false;
        }
    }
}