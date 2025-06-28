package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.*;
import fr.esgi.color_run.repository.MemberRepository;
import fr.esgi.color_run.repository.OrganizerRequestRepository;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.OrganizerRequestService;
import fr.esgi.color_run.util.RepositoryFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OrganizerRequestServiceImpl implements OrganizerRequestService {

    private final OrganizerRequestRepository organizerRequestRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    // Cache simple pour éviter les requêtes répétées
    private final ConcurrentHashMap<String, List<OrganizerRequest>> requestCache = new ConcurrentHashMap<>();
    private volatile long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 30000; // 30 secondes

    public OrganizerRequestServiceImpl() {
        RepositoryFactory factory = RepositoryFactory.getInstance();
        this.organizerRequestRepository = factory.getOrganizerRequestRepository();
        this.memberRepository = factory.getMemberRepository();
        this.memberService = new MemberServiceImpl();
    }

    @Override
    public void submitRequest(Long memberId, RequestType requestType, String motivation, Long existingAssociationId) {
        System.out.println("📝 OrganizerRequestService.submitRequest() appelé");

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

            organizerRequestRepository.save(request);

            // Invalider le cache
            clearCache();

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
                throw new IllegalStateException("Le membre a déjà une demande en cours ou ne peut pas soumettre de demande");
            }

            // Validations (même logique que l'original)
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

            OrganizerRequest savedRequest = organizerRequestRepository.save(request);

            // Invalider le cache
            clearCache();

            if (savedRequest != null && savedRequest.getId() != null) {
                System.out.println("✅ Service - Demande avec nouvelle association créée avec ID: " + savedRequest.getId());
            } else {
                throw new RuntimeException("Échec de la sauvegarde - aucun ID généré");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans submitRequestWithNewAssociation:");
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de la demande avec nouvelle association", e);
        }
    }

    @Override
    public List<OrganizerRequest> getAllRequests() throws Exception {
        return getCachedRequests("all", () -> {
            System.out.println("🔍 Service - Récupération de toutes les demandes (depuis DB)");
            List<OrganizerRequest> requests = organizerRequestRepository.findAll();

            // Optimisation: charger les rôles en batch plutôt qu'individuellement
            enrichRequestsWithMemberRoles(requests);

            System.out.println("🔍 Service - " + requests.size() + " demandes totales trouvées");
            return requests;
        });
    }

    @Override
    public List<OrganizerRequest> getPendingRequests() {
        return getCachedRequests("pending", () -> {
            System.out.println("🔍 Service - Récupération des demandes en attente (depuis DB)");
            List<OrganizerRequest> requests = organizerRequestRepository.findByStatus(RequestStatus.PENDING);

            // Optimisation: charger les rôles en batch plutôt qu'individuellement
            enrichRequestsWithMemberRoles(requests);

            System.out.println("🔍 Service - " + requests.size() + " demandes en attente trouvées");
            return requests;
        });
    }

    /**
     * Optimisation: enrichir toutes les demandes en une seule fois avec les rôles des membres
     */
    private void enrichRequestsWithMemberRoles(List<OrganizerRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        // Créer une map des membres pour éviter les requêtes multiples
        ConcurrentHashMap<Long, String> memberRoles = new ConcurrentHashMap<>();

        for (OrganizerRequest request : requests) {
            if (request != null && request.getMemberId() != null) {
                memberRoles.computeIfAbsent(request.getMemberId(), memberId -> {
                    try {
                        Optional<Member> memberOpt = memberService.getMember(memberId);
                        return memberOpt.map(member -> member.getRole().name()).orElse("UNKNOWN");
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors de la récupération du rôle pour membre " + memberId + ": " + e.getMessage());
                        return "UNKNOWN";
                    }
                });

                request.setMemberRoleName(memberRoles.get(request.getMemberId()));
            }
        }
    }

    /**
     * Méthode utilitaire pour le cache
     */
    private List<OrganizerRequest> getCachedRequests(String cacheKey, RequestSupplier supplier) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCacheUpdate < CACHE_DURATION && requestCache.containsKey(cacheKey)) {
            System.out.println("✅ Utilisation du cache pour " + cacheKey);
            return requestCache.get(cacheKey);
        }

        try {
            List<OrganizerRequest> requests = supplier.get();
            requestCache.put(cacheKey, requests);
            lastCacheUpdate = currentTime;
            return requests;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des demandes: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void clearCache() {
        requestCache.clear();
        lastCacheUpdate = 0;
        System.out.println("🗑️ Cache invalidé");
    }

    @FunctionalInterface
    private interface RequestSupplier {
        List<OrganizerRequest> get() throws Exception;
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

        // Invalider le cache
        clearCache();

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

        // Invalider le cache
        clearCache();

        System.out.println("✅ Service - Demande " + requestId + " rejetée");
        return request;
    }

    @Override
    public Optional<OrganizerRequest> getRequestById(Long requestId) {
        System.out.println("🔍 Service - Récupération de la demande par ID: " + requestId);
        return organizerRequestRepository.findById(requestId);
    }

    @Override
    public void deleteRequest(Long requestId) {
        System.out.println("🔍 Service - Suppression de la demande ID: " + requestId);
        organizerRequestRepository.deleteById(requestId);

        // Invalider le cache
        clearCache();
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

}
