package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.impl.Association_memberRepositoryImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Association_memberRepository {
    void addOrganizerToAssociation(Long memberId, Long associationId);

    void removeOrganizerFromAssociation(Long memberId, Long associationId);

    boolean isOrganizerInAssociation(Long memberId, Long associationId);

    //List<Association> findAssociationByOrganizerId(Long memberId);

    List<Association> findAssociationsByOrganizerId(Long memberId);

    List<Member> findOrganizersByAssociationId(Long associationId);

    boolean organizerHasAssociation(Long memberId);

    int countOrganizersByAssociationId(Long associationId);

    List<Association> findAvailableAssociationsForMember(Long memberId) throws SQLException;

    List<Association_memberRepositoryImpl.AssociationMemberDetail> getAllAssociationMembers();

    void changeOrganizerAssociation(Long memberId, Long newAssociationId);
}
