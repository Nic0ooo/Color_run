package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.repository.Association_memberRepository;
import fr.esgi.color_run.repository.impl.Association_memberRepositoryImpl;
import fr.esgi.color_run.service.Association_memberService;
import fr.esgi.color_run.service.AssociationService;
import fr.esgi.color_run.service.MemberService;

import java.util.List;
import java.util.Optional;

public class Association_memberServiceImpl implements Association_memberService {

    private final Association_memberRepository associationMemberRepository = new Association_memberRepositoryImpl();
    private final MemberService memberService = new MemberServiceImpl();
    private final AssociationService associationService = new AssociationServiceImpl();

    @Override
    public void addOrganizerToAssociation(Long memberId, Long associationId) {
        // Validation
        Optional<Member> memberOpt = memberService.getMember(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Membre introuvable");
        }

        Member member = memberOpt.get();
        if (member.getRole() != Role.ORGANIZER) {
            throw new IllegalArgumentException("Seuls les organisateurs peuvent rejoindre une association");
        }

        Optional<Association> associationOpt = associationService.getAssociationById(associationId);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Association introuvable");
        }

        associationMemberRepository.addOrganizerToAssociation(memberId, associationId);
    }

    @Override
    public void removeOrganizerFromAssociation(Long memberId) {
        // Validation
        Optional<Member> memberOpt = memberService.getMember(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Membre introuvable");
        }

        associationMemberRepository.removeOrganizerFromAssociation(memberId);
    }

    @Override
    public boolean isOrganizerInAssociation(Long memberId, Long associationId) {
        return associationMemberRepository.isOrganizerInAssociation(memberId, associationId);
    }

    @Override
    public Optional<Association> getOrganizerAssociation(Long memberId) {
        return associationMemberRepository.findAssociationByOrganizerId(memberId);
    }

    @Override
    public List<Member> getOrganizersByAssociation(Long associationId) {
        return associationMemberRepository.findOrganizersByAssociationId(associationId);
    }

    @Override
    public boolean organizerHasAssociation(Long memberId) {
        return associationMemberRepository.organizerHasAssociation(memberId);
    }

    @Override
    public int countOrganizersByAssociation(Long associationId) {
        return associationMemberRepository.countOrganizersByAssociationId(associationId);
    }

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
    }
}