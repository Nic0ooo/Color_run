package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Course;

import java.time.LocalDate;
import java.util.List;

public interface CourseService {
    List<Course> listAllCourses();

    List<Course> listUpcomingCourses();

    List<Course> listPastCourses();

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
    List<Course> searchCourseByName(String name);

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

    /**
     * Recherche et trie les courses créées par un membre spécifique
     * @param searchTerm terme de recherche (nom, ville, code postal)
     * @param fromDate date de début (optionnel)
     * @param toDate date de fin (optionnel)
     * @param sortBy colonne de tri (name, startDate, city, distance)
     * @param sortDirection direction du tri (asc, desc)
     * @param upcoming true pour les courses à venir, false pour les courses passées
     * @param creatorId ID du membre créateur
     * @return liste des courses filtrées et triées
     */
    List<Course> searchAndSortCoursesByCreator(String searchTerm, LocalDate fromDate, LocalDate toDate,
                                               String sortBy, String sortDirection, boolean upcoming, Long creatorId);

    /**
     * Récupérer les courses d'une association spécifique
     */
    List<Course> getCoursesByAssociationId(Long associationId) throws Exception;

    /**
     * Filtrer les courses à venir d'une liste existante
     */
    List<Course> filterUpcomingCourses(List<Course> courses);

    /**
     * Filtrer les courses passées d'une liste existante
     */
    List<Course> filterPastCourses(List<Course> courses);

    void deleteCourse(Long id);

}
