package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Course;

import java.util.List;

public interface CourseService {
    List<Course> listAllCourses();

    /**
     * Recherche des courses à proximité d'un point géographique avec un filtre de date
     */
    List<Course> findCoursesByProximity(double latitude, double longitude, int radiusInKm, String dateFilter);

    /**
     * Recherche des courses par code postal avec un filtre de date
     */
    List<Course> findCoursesByPostalCodeAndDate(String postalCode, String dateFilter);

    /**
     * Recherche avancée des courses par code postal, rayon et plage de dates
     */
    List<Course> findCoursesByPostalCodeAndDate(String postalCode, String dateFilter,
                                                int radiusInKm, String startDate, String endDate);


    Course createCourse(Course course);

    Course updateCourse(Course course);

    Course getCourseById(Long id);
}
