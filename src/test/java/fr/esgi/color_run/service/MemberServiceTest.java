package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberServiceTest {

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
    }

    @Test
    void testFindByEmail_shouldReturnMemberIfExists() {
        Member dummyMember = new Member();
        dummyMember.setEmail("test@example.com");

        when(memberService.findByEmail("test@example.com")).thenReturn(Optional.of(dummyMember));

        Optional<Member> result = memberService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(memberService, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testExistsByEmail_shouldReturnTrueIfMemberExists() {
        when(memberService.existsByEmail("existing@mail.com")).thenReturn(true);

        boolean exists = memberService.existsByEmail("existing@mail.com");

        assertTrue(exists);
        verify(memberService).existsByEmail("existing@mail.com");
    }

    @Test
    void testUpdatePassword_shouldBeCalled() {
        doNothing().when(memberService).updatePasswordByEmail("test@mail.com", "newpassword");

        memberService.updatePasswordByEmail("test@mail.com", "newpassword");

        verify(memberService).updatePasswordByEmail("test@mail.com", "newpassword");
    }
}
