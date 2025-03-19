package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    List<Member> findAll();
    Boolean deleteById(Long id);
    Member update(Member member);
}
