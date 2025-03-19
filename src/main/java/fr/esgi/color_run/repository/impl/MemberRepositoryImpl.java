package fr.esgi.color_run.repository.impl;


import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberRepositoryImpl implements MemberRepository {
    private final List<Member> members = new ArrayList<>();

    @Override
    public Member save(Member member) {
        Optional<Member> existingMember = findById(member.getId());

        if (existingMember.isPresent()) {
            members.remove(existingMember.get());
        }
        members.add(member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return members.stream().filter(member -> member.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return members.stream().filter(member -> member.getEmail().equalsIgnoreCase(email.trim())).findFirst();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members);
    }

    @Override
    public Boolean deleteById(Long id) {
        return members.removeIf(member -> member.getId().equals(id));
    }

    @Override
    public Member update(Member updatedMember) {
        Optional<Member> existingMemberOpt = findById(updatedMember.getId());
        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            existingMember.setName(updatedMember.getName());
            existingMember.setFirstname(updatedMember.getFirstname());
            existingMember.setEmail(updatedMember.getEmail());
            existingMember.setPassword(updatedMember.getPassword());
            existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
            existingMember.setAddress(updatedMember.getAddress());
            existingMember.setCity(updatedMember.getCity());
            existingMember.setZipCode(updatedMember.getZipCode());
            existingMember.setPositionLatitude(updatedMember.getPositionLatitude());
            existingMember.setPositionLongitude(updatedMember.getPositionLongitude());
            return existingMember;
        }
        return null;
    }
}
