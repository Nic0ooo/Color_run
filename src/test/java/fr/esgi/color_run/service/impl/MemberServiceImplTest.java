package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceImplTest {

    private MemberRepository memberRepository;
    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        memberService = new MemberServiceImpl(memberRepository); // Ce constructeur doit exister
    }

    @Test
    void testCreateMember_shouldHashPassword() {
        Member rawMember = new Member();
        rawMember.setEmail("test@example.com");
        rawMember.setPassword("plainPassword");
        rawMember.setRole(Role.RUNNER);

        when(memberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Member saved = memberService.createMember(rawMember);

        assertNotNull(saved);
        assertTrue(BCrypt.checkpw("plainPassword", saved.getPassword()));
        verify(memberRepository).save(any());
    }

    @Test
    void testConnectMember_validPassword_shouldReturnMember() {
        String rawPassword = "secret";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        Member member = new Member();
        member.setEmail("user@mail.com");
        member.setPassword(hashed);

        when(memberRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.connectMember("user@mail.com", rawPassword);

        assertTrue(result.isPresent());
        assertEquals(member, result.get());
    }

    @Test
    void testConnectMember_invalidPassword_shouldReturnEmpty() {
        Member member = new Member();
        member.setEmail("user@mail.com");
        member.setPassword(BCrypt.hashpw("correct", BCrypt.gensalt()));

        when(memberRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.connectMember("user@mail.com", "wrong");

        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteMember_shouldCallRepository() {
        when(memberRepository.deleteById(1L)).thenReturn(true);

        boolean deleted = memberService.deleteMember(1L);

        assertTrue(deleted);
        verify(memberRepository).deleteById(1L);
    }

    @Test
    void testUpdateMember_shouldCallRepository() {
        Member updated = new Member();
        updated.setName("new");
        when(memberRepository.update(updated)).thenReturn(updated);

        Member result = memberService.updateMember(2L, updated);

        assertEquals("new", result.getName());
        assertEquals(2L, updated.getId());
        verify(memberRepository).update(updated);
    }

    @Test
    void testListAllMembers() {
        List<Member> members = List.of(new Member(), new Member());
        when(memberRepository.findAll()).thenReturn(members);

        List<Member> result = memberService.listAllMembers();

        assertEquals(2, result.size());
    }

    @Test
    void testGetMember_existingId() {
        Member m = new Member();
        m.setId(10L);
        when(memberRepository.findById(10L)).thenReturn(Optional.of(m));

        Optional<Member> result = memberService.getMember(10L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void testExistsByEmail_true() {
        when(memberRepository.findByEmail("abc@xyz.com")).thenReturn(Optional.of(new Member()));

        assertTrue(memberService.existsByEmail("abc@xyz.com"));
    }

    @Test
    void testFindByEmail_shouldDelegateToRepo() {
        Member member = new Member();
        when(memberRepository.findByEmail("email@test.com")).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.findByEmail("email@test.com");

        assertTrue(result.isPresent());
        verify(memberRepository).findByEmail("email@test.com");
    }

    @Test
    void testUpdatePasswordByEmail_shouldHashPassword() {
        String email = "u@x.com";
        String rawPassword = "abc123";

        memberService.updatePasswordByEmail(email, rawPassword);

        verify(memberRepository).updatePasswordByEmail(eq(email), argThat(hashed -> !hashed.equals(rawPassword) && BCrypt.checkpw(rawPassword, hashed)));
    }

    @Test
    void testUpdateRole_shouldUpdateWhenFound() {
        Member m = new Member();
        m.setId(3L);
        m.setRole(Role.RUNNER);

        when(memberRepository.findById(3L)).thenReturn(Optional.of(m));
        when(memberRepository.update(any())).thenReturn(m);

        memberService.updateRole(3L, "ADMIN");

        assertEquals(Role.ADMIN, m.getRole());
        verify(memberRepository).update(m);
    }

    @Test
    void testGenerateVerificationCodeForEmail_shouldReturnSixDigits() {
        String code = memberService.generateVerificationCodeForEmail("x@y.com");

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }
}
