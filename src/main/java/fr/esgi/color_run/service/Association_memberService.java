package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Association_member;
import java.util.List;

public interface Association_memberService {

     // Ajouter un organisateur à une association
    void addOrganizerToAssociation(Long memberId, Long associationId) throws Exception;

    void removeOrganizerFromAssociation(Long memberId, Long associationId);

    boolean isOrganizerInAssociation(Long memberId, Long associationId);

    List<Member> getOrganizersByAssociation(Long associationId) throws Exception;

    int getAssociationOrganizerCount(Long associationId) throws Exception;

    List<Association_member> getAllAssociationMembers() throws Exception;

    List<Association> getAvailableAssociationForMember(Long memberId) throws Exception;

    List<Association> getAssociationsByOrganizer(Long memberId) throws Exception;
}