package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;

import java.util.List;

public interface Course_memberRepository {

    void save(Course_member course_member);

    void delete(Course_member course_member);

    boolean isMemberInCourse(long courseId, long memberId);

    List<Course> findCoursesByMemberId(long memberId);

    List<Course> findPastCoursesByMemberId(long memberId);

    List<Course> findUpcomingCoursesByMemberId(long memberId);

    List<Member> findMembersByCourseId(long courseId);
}
