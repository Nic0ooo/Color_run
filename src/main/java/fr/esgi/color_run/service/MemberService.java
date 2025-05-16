package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Member;
import java.util.List;
import java.util.Optional;

public interface MemberService {
    Member createMember(Member member);
    Optional<Member> connectMember(String mail, String password);
    boolean deleteMember(Long id);
    Member updateMember(Long id, Member updatedMember);
    List<Member> listAllMembers();
    Optional<Member> getMember(Long id);
    String generateVerificationCodeForEmail(String email);
    Optional<Member> findByEmail(String email);
    void updatePasswordByEmail(String email, String newPassword);
    boolean existsByEmail(String email);

}
