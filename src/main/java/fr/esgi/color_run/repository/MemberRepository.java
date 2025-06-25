package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    List<Member> findAll();
    Boolean deleteById(Long id);
    Member update(Member member);
    void updatePasswordByEmail(String email, String password);
    List<Member> findByRole(Role role);
    void updateMemberRole(Long memberId, Role newRole);
    List<Member> findOrganizersByAssociationId(Long associationId);
    int countOrganizersByAssociationId(Long associationId);
}
