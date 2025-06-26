package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.business.GeoLocation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final GeocodingService geocodingService;

    public CourseServiceImpl(CourseRepository courseRepository, GeocodingService geocodingService) {
        this.courseRepository = courseRepository;
        this.geocodingService = geocodingService;
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
    public List<Course> findCoursesByProximity(double latitude, double longitude, int radiusInKm, String dateFilter) {
        //Récupérer les courses dans le rayon spécifié via le repository
        List<Course> proximityResults = courseRepository.findByProximity(latitude, longitude, radiusInKm);

        // Si aucun filtre de date ou "all", retourner tous els resultats
        if (dateFilter == null || dateFilter.equals("all")) {
            return proximityResults;
        }

        // Filtrer les résultats par date
        return proximityResults.stream()
                .filter(course -> course.getMonthName().equalsIgnoreCase(dateFilter))
                .collect(Collectors.toList());
    }

    // Ajouter cette méthode dans CourseServiceImpl.java dans la méthode findCoursesByPostalCodeAndDate

    @Override
    public List<Course> findCoursesByPostalCodeAndDate(String postalCode, String dateFilter, int radiusInKm,
                                                       String startDate, String endDate) {
        System.out.println("=== CourseServiceImpl.findCoursesByPostalCodeAndDate ===");
        System.out.println("Paramètres:");
        System.out.println("  - postalCode: '" + postalCode + "'");
        System.out.println("  - dateFilter: '" + dateFilter + "'");
        System.out.println("  - radiusInKm: " + radiusInKm);
        System.out.println("  - startDate: '" + startDate + "'");
        System.out.println("  - endDate: '" + endDate + "'");

        List<Course> filteredCourses;

        // Si radiusInKm > 0, utiliser la recherche par proximité
        if (postalCode != null && !postalCode.isEmpty() && !postalCode.equals("null") && radiusInKm > 0) {
            System.out.println("Recherche par proximité (rayon: " + radiusInKm + " km)");

            // Convertir le code postal en coordonnées géographiques
            GeoLocation location = geocodingService.getCoordinatesFromPostalCode(postalCode);
            System.out.println("Coordonnées du code postal " + postalCode + ": " + location.getLatitude() + ", " + location.getLongitude());

            filteredCourses = courseRepository.findByProximity(
                    location.getLatitude(), location.getLongitude(), radiusInKm);
            System.out.println("Courses trouvées par proximité: " + filteredCourses.size());
        } else if (postalCode != null && !postalCode.isEmpty() && !postalCode.equals("null")) {
            System.out.println("Recherche classique par code postal exacte");
            // Sinon recherche classique par code postal
            filteredCourses = courseRepository.findByPostalCode(postalCode);
            System.out.println("Courses trouvées par code postal: " + filteredCourses.size());
        } else {
            System.out.println("Recherche de toutes les courses (aucun code postal fourni)");
            filteredCourses = courseRepository.findAll();
            System.out.println("Toutes les courses: " + filteredCourses.size());
        }

        System.out.println("Courses avant filtrage par date:");
        filteredCourses.forEach(course -> System.out.println("  - " + course.getName() + " (" + course.getCity() + ", " + course.getZipCode() + ") - " + course.getStartDate()));

        // Appliquer le filtre par mois si nécessaire
        if (dateFilter != null && !dateFilter.isEmpty() && !"all".equals(dateFilter) && !dateFilter.equals("null")) {
            System.out.println("Application du filtre par mois: " + dateFilter);
            filteredCourses = filteredCourses.stream()
                    .filter(course -> course.getMonthName().equalsIgnoreCase(dateFilter))
                    .collect(Collectors.toList());
            System.out.println("Courses après filtrage par mois: " + filteredCourses.size());
        }

        // Appliquer le filtre par plage de dates si nécessaire
        if (startDate != null && !startDate.isEmpty() && !startDate.equals("null") &&
                endDate != null && !endDate.isEmpty() && !endDate.equals("null")) {
            System.out.println("Application du filtre par plage de dates: " + startDate + " à " + endDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            filteredCourses = filteredCourses.stream()
                    .filter(course -> {
                        if (course.getStartDate() == null) {
                            return false;
                        }
                        try {
                            LocalDate courseDate = course.getStartDate().toLocalDate();
                            return !courseDate.isBefore(start) && !courseDate.isAfter(end);
                        } catch (Exception e) {
                            System.out.println("Erreur de parsing de date pour la course: " + course.getName() + " - " + e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            System.out.println("Courses après filtrage par plage de dates: " + filteredCourses.size());
        }

        System.out.println("Résultat final: " + filteredCourses.size() + " courses");
        filteredCourses.forEach(course -> System.out.println("  - " + course.getName() + " (" + course.getCity() + ", " + course.getZipCode() + ")"));
        System.out.println("=== FIN CourseServiceImpl.findCoursesByPostalCodeAndDate ===");

        return filteredCourses;
    }


    // Compatibilité avec l'ancienne méthode
    @Override
    public List<Course> findCoursesByPostalCodeAndDate(String postalCode, String dateFilter) {
        return findCoursesByPostalCodeAndDate(postalCode, dateFilter, 0, null, null);
    }

    @Override
    public Course createCourse(Course course) {
        // Si l'associationId est 0, le mettre à null
        if (course.getAssociationId() != null && course.getAssociationId() == 0) {
            course.setAssociationId(null);
        }
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

    @Override
    public Course getCourseById(Long id) {
        Course course = courseRepository.findById(id);

        if (course != null) {
            System.out.println("Course found: " + course);
        } else {
            System.out.println("No course found with ID: " + id);
        }
        return course;
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