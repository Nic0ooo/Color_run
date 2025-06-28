package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Course;

import java.time.LocalDate;
import java.util.List;

public interface CourseRepository {

    List<Course> findAll();

    List<Course> findUpcomingCourses();

    List<Course> findPastCourses();

    List<Course> searchCourseByName(String name);

/*
    List<Course> getAllCourses();
*/

    List<Course> findByProximity(double latitude, double longitude, int radiusInKm);

    List<Course> findByPostalCode(String postalCode);

    List<Course> findByMonth(String monthName);

    Course save(Course course);

    Course updateCourse(Course course);

    Course findById(Long id);

    /**
     * Recherche et trie les courses selon les critères donnés
     * @param searchTerm terme de recherche (nom, ville, code postal)
     * @param fromDate date de début (optionnel)
     * @param toDate date de fin (optionnel)
     * @param sortBy colonne de tri (name, startDate, city, distance)
     * @param sortDirection direction du tri (asc, desc)
     * @param upcoming true pour les courses à venir, false pour les courses passées
     * @return liste des courses filtrées et triées
     */
    List<Course> searchAndSortCourses(String searchTerm, LocalDate fromDate, LocalDate toDate,
                                      String sortBy, String sortDirection, boolean upcoming);

    List<Course> findPastCoursesByAssociationId(Long associationId);

    List<Course> findUpcomingCoursesByAssociationId(Long associationId);
}