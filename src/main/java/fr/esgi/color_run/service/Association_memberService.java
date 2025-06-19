package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;

import java.util.List;
import java.util.Optional;

public interface Association_memberService {
    void addOrganizerToAssociation(Long memberId, Long associationId);

    void removeOrganizerFromAssociation(Long memberId);

    boolean isOrganizerInAssociation(Long memberId, Long associationId);

    Optional<Association> getOrganizerAssociation(Long memberId);

    List<Member> getOrganizersByAssociation(Long associationId);

    boolean organizerHasAssociation(Long memberId);

    int countOrganizersByAssociation(Long associationId);

    void changeOrganizerAssociation(Long memberId, Long newAssociationId);
}
