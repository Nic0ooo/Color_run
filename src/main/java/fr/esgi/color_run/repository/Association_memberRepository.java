package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;

import java.util.List;
import java.util.Optional;

public interface Association_memberRepository {
    void addOrganizerToAssociation(Long memberId, Long associationId);

    void removeOrganizerFromAssociation(Long memberId);

    boolean isOrganizerInAssociation(Long memberId, Long associationId);

    Optional<Association> findAssociationByOrganizerId(Long memberId);

    List<Member> findOrganizersByAssociationId(Long associationId);

    boolean organizerHasAssociation(Long memberId);

    int countOrganizersByAssociationId(Long associationId);

    void changeOrganizerAssociation(Long memberId, Long newAssociationId);
}
