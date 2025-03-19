package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Member;

import java.util.List;

public interface MemberRepository {
    List<Member> findAll();
    Member findById(Long id);
    void save(Member user);
    void update(Member user);
    void deleteById(Long id);
}
