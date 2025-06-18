package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import java.util.Optional;

import java.util.List;

public interface Course_memberService {
    void save(Course_member course_member);

    void delete(Course_member course_member);

    boolean isMemberInCourse(long courseId, long memberId);

    /**
     * Vérifie si un membre est inscrit ET a payé pour une course
     */
    boolean isMemberRegisteredAndPaid(long courseId, long memberId);

    /**
     * Obtient les détails de l'inscription d'un membre pour une course
     */
    Optional<Course_member> getRegistrationDetails(long courseId, long memberId);

    List<Course> findCoursesByMemberId(long memberId);

    List<Course> findPastCoursesByMemberId(long memberId);

    List<Course> findUpcomingCoursesByMemberId(long memberId);

    List<Member> findMembersByCourseId(long courseId);

    Optional<Course_member> findByStripeSessionId(String stripeSessionId);
}
