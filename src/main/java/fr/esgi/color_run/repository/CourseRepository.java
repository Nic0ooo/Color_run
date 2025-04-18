package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Course;

import java.util.List;

public interface CourseRepository {
    List<Course> findAll();

     List<Course> findUpcomingCourses();

     List <Course> findPastCourses();

    List <Course> searchCourseByName(String name);

    Course findById(Integer id);

    Course save(Course course);

    Course updateCourse(Course course);
}
