package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Member;

import java.util.List;

public interface MemberService {
    List<Member> getAllUsers();
    Member getUserById(Long id);
    void addUser(Member user);
    void updateUser(Member user);
    void deleteUser(Long id);
}
