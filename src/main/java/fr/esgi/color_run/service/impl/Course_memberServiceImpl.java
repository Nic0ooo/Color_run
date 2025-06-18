package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.Course_memberRepository;
import fr.esgi.color_run.service.Course_memberService;

import fr.esgi.color_run.repository.impl.Course_memberRepositoryImpl;
import java.util.Optional;
import java.util.List;

public class Course_memberServiceImpl implements Course_memberService {
    private final Course_memberRepository course_memberRepository;

    public Course_memberServiceImpl(Course_memberRepository course_memberRepository) {
        this.course_memberRepository = course_memberRepository;
    }
    // Constructeur par défaut pour les servlets
    public Course_memberServiceImpl() {
        this.course_memberRepository = new Course_memberRepositoryImpl();
    }

    @Override
    public void save(Course_member course_member) {
        course_memberRepository.save(course_member);
    }

    @Override
    public void delete(Course_member course_member) {
        if (course_member == null) {
            System.out.println("Course_member object is null. Cannot delete.");
            return;
        }
        course_memberRepository.delete(course_member);
    }

    @Override
    public boolean isMemberInCourse(long courseId, long memberId) {
        if (courseId <= 0 || memberId <= 0) {
            System.out.println("Invalid course ID or member ID.");
            return false;
        }
        return course_memberRepository.isMemberInCourse(courseId, memberId);
    }

    @Override
    public boolean isMemberRegisteredAndPaid(long courseId, long memberId) {
        if (courseId <= 0 || memberId <= 0) {
            System.out.println("Invalid course ID or member ID for payment check.");
            return false;
        }

        // Cast vers l'implémentation pour accéder aux méthodes Stripe
        if (course_memberRepository instanceof Course_memberRepositoryImpl) {
            return ((Course_memberRepositoryImpl) course_memberRepository)
                    .isMemberRegisteredAndPaid(courseId, memberId);
        }

        System.err.println("❌ Repository ne supporte pas isMemberRegisteredAndPaid");
        return false;
    }

    @Override
    public Optional<Course_member> getRegistrationDetails(long courseId, long memberId) {
        if (courseId <= 0 || memberId <= 0) {
            System.out.println("Invalid course ID or member ID for registration details.");
            return Optional.empty();
        }

        // Cast vers l'implémentation pour accéder aux méthodes Stripe
        if (course_memberRepository instanceof Course_memberRepositoryImpl) {
            return ((Course_memberRepositoryImpl) course_memberRepository)
                    .getRegistrationDetails(courseId, memberId);
        }

        System.err.println("❌ Repository ne supporte pas getRegistrationDetails");
        return Optional.empty();
    }


    @Override
    public List<Course> findCoursesByMemberId(long memberId) {
        List<Course> coursesOfMember = course_memberRepository.findCoursesByMemberId(memberId);
        if (coursesOfMember.isEmpty()) {
            System.out.println("No courses found for member ID: " + memberId);
        } else {
            System.out.println("Courses found for member ID " + memberId + ": " + coursesOfMember);
        }
        return coursesOfMember;
    }

    @Override
    public List<Course> findPastCoursesByMemberId(long memberId) {
        List<Course> coursesOfMember = course_memberRepository.findPastCoursesByMemberId(memberId);
        if (coursesOfMember.isEmpty()) {
            System.out.println("No courses found for member ID: " + memberId);
        } else {
            System.out.println("Courses found for member ID " + memberId + ": " + coursesOfMember);
        }
        return coursesOfMember;
    }

    @Override
    public List<Course> findUpcomingCoursesByMemberId(long memberId) {
        List<Course> coursesOfMember = course_memberRepository.findUpcomingCoursesByMemberId(memberId);
        if (coursesOfMember.isEmpty()) {
            System.out.println("No courses found for member ID: " + memberId);
        } else {
            System.out.println("Courses found for member ID " + memberId + ": " + coursesOfMember);
        }
        return coursesOfMember;
    }




    @Override
    public List<Member> findMembersByCourseId(long courseId) {
        List<Member> memberOfCourse = course_memberRepository.findMembersByCourseId(courseId);
        if (memberOfCourse.isEmpty()) {
            System.out.println("No members found for course ID: " + courseId);
        } else {
            System.out.println("Members found for course ID " + courseId + ": " + memberOfCourse);
        }
        return memberOfCourse;
    }

    @Override
    public Optional<Course_member> findByStripeSessionId(String stripeSessionId) {
        // Cast vers l'implémentation pour accéder à la méthode spécifique
        if (course_memberRepository instanceof Course_memberRepositoryImpl) {
            return ((Course_memberRepositoryImpl) course_memberRepository).findByStripeSessionId(stripeSessionId);
        }
        System.err.println("❌ Repository ne supporte pas findByStripeSessionId");
        return Optional.empty();
    }
}
