package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.MemberRepository;
import fr.esgi.color_run.repository.impl.MemberRepositoryImpl;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.utils.VerificationCodeStorage;
import fr.esgi.color_run.util.RepositoryFactory;

import java.util.*;

public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl() {
        RepositoryFactory factory = RepositoryFactory.getInstance();
        this.memberRepository = factory.getMemberRepository();
    }

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

    private final Map<String, String> verificationCodes = new HashMap<>();

    public String generateVerificationCodeForEmail(String email) {
        String code = String.valueOf(new Random().nextInt(900000) + 100000); // Code à 6 chiffres
        VerificationCodeStorage.storeCode(email, code);
        return code;
    }

    public boolean isCodeValid(String email, String code) {
        return code.equals(verificationCodes.get(email));
    }

    public void removeCode(String email) {
        verificationCodes.remove(email);
    }

    @Override
    public void updatePasswordByEmail(String email, String password) {
            memberRepository.updatePasswordByEmail(email, password);
    }


    @Override
    public boolean existsByEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }


    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }




}


