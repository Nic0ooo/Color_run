package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.repository.MemberRepository;
import fr.esgi.color_run.repository.impl.MemberRepositoryImpl;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.utils.VerificationCodeStorage;
import fr.esgi.color_run.util.RepositoryFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;

public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl() {
        RepositoryFactory factory = RepositoryFactory.getInstance();
        this.memberRepository = factory.getMemberRepository();
    }

    @Override
    public Member createMember(Member member) {
        String rawPassword = member.getPassword();
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        member.setPassword(hashedPassword);

        return memberRepository.save(member);
    }




    @Override
    public Optional<Member> connectMember(String mail, String password) {
        Optional<Member> memberOpt = memberRepository.findByEmail(mail);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            String hashedPassword = member.getPassword();

            // Vérification du mot de passe avec BCrypt
            if (BCrypt.checkpw(password, hashedPassword)) {
                return Optional.of(member);
            }
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
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        memberRepository.updatePasswordByEmail(email, hashed);
    }


    @Override
    public boolean existsByEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }


    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public void updateRole(Long memberId, String newRole) {
        Optional<Member> opt = memberRepository.findById(memberId);
        if (opt.isPresent()) {
            Member member = opt.get();
            member.setRole(Role.valueOf(newRole));
            memberRepository.update(member);
        }
    }




}


