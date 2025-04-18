package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseService;

import java.util.List;

public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl() {
        this.courseRepository = new CourseRepositoryImpl();
    }

    @Override
    public List<Course> listAllCourses() {
        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
        } else {
            System.out.println("Courses found: " + courses);
        }
        return courses;
    }

    @Override
    public List<Course> listUpcomingCourses() {
        List<Course> upcomingCourses = courseRepository.findUpcomingCourses();
        if (upcomingCourses.isEmpty()) {
            System.out.println("No upcoming courses found.");
        } else {
            System.out.println("Upcoming courses found: " + upcomingCourses);
        }
        return upcomingCourses;
    }

    @Override
    public List<Course> listPastCourses() {
        List<Course> pastCourses = courseRepository.findPastCourses();
        if (pastCourses.isEmpty()) {
            System.out.println("No past courses found.");
        } else {
            System.out.println("Past courses found: " + pastCourses);
        }
        return pastCourses;
    }

    @Override
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Course course) {
        Course updatedCourse = courseRepository.updateCourse(course);
        if (updatedCourse != null) {
            System.out.println("Course updated successfully: " + updatedCourse);
        } else {
            System.out.println("Failed to update course.");
        }
        return updatedCourse;
    }
}