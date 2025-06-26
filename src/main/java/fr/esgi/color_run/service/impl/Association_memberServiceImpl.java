package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Association_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.repository.Association_memberRepository;
import fr.esgi.color_run.repository.MemberRepository;
import fr.esgi.color_run.repository.impl.Association_memberRepositoryImpl;
import fr.esgi.color_run.repository.impl.MemberRepositoryImpl;
import fr.esgi.color_run.service.Association_memberService;
import fr.esgi.color_run.service.AssociationService;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.util.RepositoryFactory;

import java.util.List;
import java.util.Optional;

public class Association_memberServiceImpl implements Association_memberService {

    private final Association_memberRepository associationMemberRepository;
    private final MemberService memberService = new MemberServiceImpl();
    private final AssociationService associationService = new AssociationServiceImpl();
    private final MemberRepository memberRepository;

    public Association_memberServiceImpl() {
        RepositoryFactory factory = RepositoryFactory.getInstance();
        this.associationMemberRepository = factory.getAssociationMemberRepository();
        this.memberRepository = factory.getMemberRepository();
    }

    @Override
    public void addOrganizerToAssociation(Long memberId, Long associationId) throws Exception {
        System.out.println("🔍 Service - Ajout membre " + memberId + " à association " + associationId);

        // Validation
        Optional<Member> memberOpt = memberService.getMember(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Membre non trouvé avec ID: " + memberId);
        }

        Member member = memberOpt.get();
        if (member.getRole() != Role.ORGANIZER) {
            throw new IllegalArgumentException("Seuls les organisateurs peuvent rejoindre une association");
        }

        Optional<Association> associationOpt = associationService.getAssociationById(associationId);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Association introuvable");
        }
        Association association = associationOpt.get();

        associationMemberRepository.addOrganizerToAssociation(memberId, associationId);
        System.out.println("✅ Service - Membre " + memberId + " ajouté à association " + associationId);
    }

    @Override
    public void removeOrganizerFromAssociation(Long memberId, Long associationId) {
        System.out.println("🔍 Service - Retrait membre " + memberId + " de association " + associationId);

        // Vérifier que la relation existe
        if (!isOrganizerInAssociation(memberId, associationId)) {
            throw new IllegalStateException("Le membre ne fait pas partie de cette association");
        }

        associationMemberRepository.removeOrganizerFromAssociation(memberId, associationId);
        System.out.println("✅ Service - Membre " + memberId + " retiré de association " + associationId);
    }

    @Override
    public boolean isOrganizerInAssociation(Long memberId, Long associationId) {
        System.out.println("🔍 Service - Récupération associations pour membre " + memberId);

        // Vérifier que le membre existe
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Membre non trouvé avec ID: " + memberId);
        }

        Optional<Association> associations = associationMemberRepository.findAssociationByOrganizerId(memberId);
        if (associations.isEmpty()) {
            System.out.println("❌ Service - Aucune association trouvée pour membre " + memberId);
            return false;
        }
        System.out.println("✅ Service - " + associations.stream().count() + " associations trouvées pour membre " + memberId);
        return associations.stream().anyMatch(association -> association.getId().equals(associationId));
    }

    @Override
    public List<Member> getOrganizersByAssociation(Long associationId) throws Exception {
        System.out.println("🔍 Service - Récupération organisateurs pour association " + associationId);

        // Vérifier que l'association existe
        Optional<Association> associationOpt = associationService.getAssociationById(associationId);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Association non trouvée avec ID: " + associationId);
        }


        List<Member> organizers = associationMemberRepository.findOrganizersByAssociationId(associationId);
        System.out.println("✅ Service - " + organizers.size() + " organisateurs trouvés pour association " + associationId);
        return organizers;
    }

    @Override
    public int getAssociationOrganizerCount(Long associationId) throws Exception {
        return associationMemberRepository.countOrganizersByAssociationId(associationId);
    }

    @Override
    public List<Association_member> getAllAssociationMembers() {
        System.out.println("🔍 Service - Récupération de tous les membres d'associations");
        List<Association_memberRepositoryImpl.AssociationMemberDetail> details = associationMemberRepository.getAllAssociationMembers();

        // Convertir les détails en objets AssociationMember
        List<Association_member> members = new java.util.ArrayList<>();
        for (var detail : details) {
            Association_member member = new Association_member(detail.getMemberId(), detail.getAssociationId());
            member.setMemberId(detail.getMemberId());
            member.setAssociationId(detail.getAssociationId());
            member.setJoinDate(detail.getJoinDate());
            member.setMemberName(detail.getFullMemberName());
            member.setMemberEmail(detail.getMemberEmail());
            member.setAssociationName(detail.getAssociationName());
            members.add(member);
        }

        System.out.println("✅ Service - " + members.size() + " relations trouvées");
        return members;
    }

    @Override
    public List<Association> getAvailableAssociationForMember(Long memberId) throws Exception {
        System.out.println("🔍 Service - Récupération des associations disponibles pour le membre " + memberId);

        // Vérifier que le membre existe
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Membre non trouvé avec ID: " + memberId);
        }

        // Récupérer les associations auxquelles le membre n'est pas déjà associé
        List<Association> availableAssociations = associationMemberRepository.findAvailableAssociationsForMember(memberId);
        System.out.println("✅ Service - " + availableAssociations.size() + " associations disponibles pour le membre " + memberId);
        return availableAssociations;
    }

    @Override
    public List<Association> getAssociationsByOrganizer(Long memberId) throws Exception {
        System.out.println("🔍 Service - Récupération de l'association pour l'organisateur " + memberId);

        // Vérifier que le membre existe
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Membre non trouvé avec ID: " + memberId);
        }

        // Récupérer les associations auxquelles l'organisateur est associé
        Optional<Association> associationOpt = associationMemberRepository.findAssociationByOrganizerId(memberId);
        List<Association> associations = associationOpt.map(List::of).orElse(List.of());
        System.out.println("✅ Service - " + associations.size() + " associations trouvées pour l'organisateur " + memberId);
        return associations;
    }

/*
    @Override
    public void changeOrganizerAssociation(Long memberId, Long newAssociationId) {
        // Validation
        Optional<Member> memberOpt = memberService.getMember(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Membre introuvable");
        }

        Member member = memberOpt.get();
        if (member.getRole() != Role.ORGANIZER) {
            throw new IllegalArgumentException("Seuls les organisateurs peuvent changer d'association");
        }

        Optional<Association> associationOpt = associationService.getAssociationById(newAssociationId);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Association introuvable");
        }

        associationMemberRepository.changeOrganizerAssociation(memberId, newAssociationId);
    }*/
}