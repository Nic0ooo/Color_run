package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Course;

import java.util.List;

public interface CourseRepository {
    List<Course> findAll();
/*
    List<Course> getAllCourses();
*/

    List<Course> findByProximity(double latitude, double longitude, int radiusInKm);

    List<Course> findByPostalCode(String postalCode);

    List<Course> findByMonth(String monthName);

    Course save(Course course);

    Course updateCourse(Course course);

    Course findById(Long id);
}
