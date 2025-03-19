package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Member;
import java.util.List;
import java.util.Optional;

public interface MemberService {
    Member createMember(String pseudo, String mail, String password);
    Optional<Member> connectMember(String mail, String password);
    boolean deleteMember(Long id);
    Member updateMember(Long id, Member updatedMember);
    List<Member> listAllMembers();
    Optional<Member> getMember(Long id);
}
