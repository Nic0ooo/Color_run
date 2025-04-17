package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Course;

import java.util.List;

public interface CourseService {
    List<Course> listAllCourses();

    List<Course> listUpcomingCourses();

    List<Course> listPastCourses();

    Course createCourse(Course course);

    Course updateCourse(Course course);

}
