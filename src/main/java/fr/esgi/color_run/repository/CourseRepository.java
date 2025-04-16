package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Course;

import java.util.List;

public interface CourseRepository {
    List<Course> findAll();
/*
    List<Course> getAllCourses();
*/
    Course save(Course course);

    Course updateCourse(Course course);
}
