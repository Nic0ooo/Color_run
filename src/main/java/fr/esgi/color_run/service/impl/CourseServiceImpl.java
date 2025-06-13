package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseService;

import java.time.LocalDate;
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
            System.out.println("Courses found: " + courses.size());
        }
        return courses;
    }

    @Override
    public List<Course> listUpcomingCourses() {
        List<Course> upcomingCourses = courseRepository.findUpcomingCourses();
        if (upcomingCourses.isEmpty()) {
            System.out.println("No upcoming courses found.");
        } else {
            System.out.println("Upcoming courses found: " + upcomingCourses.size());
        }
        return upcomingCourses;
    }

    @Override
    public List<Course> listPastCourses() {
        List<Course> pastCourses = courseRepository.findPastCourses();
        if (pastCourses.isEmpty()) {
            System.out.println("No past courses found.");
        } else {
            System.out.println("Past courses found: " + pastCourses.size());
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
            System.out.println("Course updated successfully: " + updatedCourse.getName());
        } else {
            System.out.println("Failed to update course.");
        }
        return updatedCourse;
    }

    @Override
    public List<Course> searchCourseByName(String name) {
        List<Course> searchedCourses = courseRepository.searchCourseByName(name);
        if (searchedCourses.isEmpty()) {
            System.out.println("No courses found with the name: " + name);
        } else {
            System.out.println("Courses found with the name " + name + ": " + searchedCourses.size());
        }
        return searchedCourses;
    }

    @Override
    public Course getCourseById(Integer id) {
        Course course = courseRepository.findById(id);
        if (course != null) {
            System.out.println("Course found: " + course.getName());
        } else {
            System.out.println("No course found with the ID: " + id);
        }
        return course;
    }

    @Override
    public List<Course> searchAndSortCourses(String searchTerm, LocalDate fromDate, LocalDate toDate,
                                             String sortBy, String sortDirection, boolean upcoming) {

        System.out.println("Recherche de courses avec critères:");
        System.out.println("- Terme de recherche: " + searchTerm);
        System.out.println("- Date début: " + fromDate);
        System.out.println("- Date fin: " + toDate);
        System.out.println("- Tri par: " + sortBy + " (" + sortDirection + ")");
        System.out.println("- Courses à venir: " + upcoming);

        List<Course> courses = courseRepository.searchAndSortCourses(
                searchTerm, fromDate, toDate, sortBy, sortDirection, upcoming);

        System.out.println("Résultats trouvés: " + courses.size() + " courses");
        return courses;
    }
}