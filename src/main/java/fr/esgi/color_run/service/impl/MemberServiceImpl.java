package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.MemberRepository;
import fr.esgi.color_run.repository.impl.MemberRepositoryImpl;
import fr.esgi.color_run.service.MemberService;

import java.util.List;
import java.util.Optional;

public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository = new MemberRepositoryImpl();

    @Override
    public Member createMember(Member member) {
        return memberRepository.save(member);
    }



    @Override
    public Optional<Member> connectMember(String mail, String password) {
        Optional<Member> memberOpt = memberRepository.findByEmail(mail);
        if (memberOpt.isPresent() && memberOpt.get().getPassword().equals(password)) {
            return memberOpt;
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteMember(Long id) {
        return memberRepository.deleteById(id);
    }

    @Override
    public Member updateMember(Long id, Member updatedMember) {
        updatedMember.setId(id);
        return memberRepository.update(updatedMember);
    }

    @Override
    public List<Member> listAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public Optional<Member> getMember(Long id) {
        return memberRepository.findById(id);
    }
}
