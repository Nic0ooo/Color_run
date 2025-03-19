package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.service.MemberService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberServiceImpl implements MemberService {
    private final List<Member> members = new ArrayList<>();

    @Override
    public Member createMember(String pseudo, String mail, String password) {
        Member member = new Member();
        member.setId((long) (members.size() + 1));
        member.setName(pseudo);
        member.setEmail(mail);
        member.setPassword(password);
        members.add(member);
        return member;
    }

    @Override
    public Optional<Member> connectMember(String mail, String password) {
        return members.stream()
                .filter(member -> member.getEmail().trim().equalsIgnoreCase(mail.trim()) && member.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public boolean deleteMember(Long id) {
        return members.removeIf(member -> member.getId().equals(id));
    }

    @Override
    public Member updateMember(Long id, Member updatedMember) {
        Optional<Member> memberOpt = getMember(id);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            member.setName(updatedMember.getName());
            member.setFirstname(updatedMember.getFirstname());
            member.setEmail(updatedMember.getEmail());
            member.setPassword(updatedMember.getPassword());
            member.setPhoneNumber(updatedMember.getPhoneNumber());
            member.setAddress(updatedMember.getAddress());
            member.setCity(updatedMember.getCity());
            member.setZipCode(updatedMember.getZipCode());
            member.setPositionLatitude(updatedMember.getPositionLatitude());
            member.setPositionLongitude(updatedMember.getPositionLongitude());
            return member;
        }
        return null;
    }

    @Override
    public List<Member> listAllMembers() {
        return members;
    }

    @Override
    public Optional<Member> getMember(Long id) {
        return members.stream().filter(member -> member.getId().equals(id)).findFirst();
    }
}
