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

    @Override
    public List<Course> findCoursesByPostalCodeAndDate(String postalCode, String dateFilter, int radiusInKm,
                                                       String startDate, String endDate) {
        List<Course> filteredCourses;

        // Si radiusInKm > 0, utiliser la recherche par proximité
        if (postalCode != null && !postalCode.isEmpty() && radiusInKm > 0) {
            // Convertir le code postal en coordonnées géographiques
            GeoLocation location = geocodingService.getCoordinatesFromPostalCode(postalCode);
            filteredCourses = courseRepository.findByProximity(
                    location.getLatitude(), location.getLongitude(), radiusInKm);
        } else {
            // Sinon recherche classique par code postal
            filteredCourses = courseRepository.findByPostalCode(postalCode);
        }

        // Appliquer le filtre par mois si nécessaire
        if (dateFilter != null && !dateFilter.isEmpty() && !"all".equals(dateFilter)) {
            filteredCourses = filteredCourses.stream()
                    .filter(course -> course.getMonthName().equalsIgnoreCase(dateFilter))
                    .collect(Collectors.toList());
        }

        // Appliquer le filtre par plage de dates si nécessaire
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            filteredCourses = filteredCourses.stream()
                    .filter(course -> {
                        if (course.getStartDate() == null || course.getStartDate().isEmpty()) {
                            return false;
                        }
                        LocalDate courseDate = LocalDate.parse(course.getStartDate(), formatter);
                        return !courseDate.isBefore(start) && !courseDate.isAfter(end);
                    })
                    .collect(Collectors.toList());
        }

        return filteredCourses;
    }

    // Compatibilité avec l'ancienne méthode
    @Override
    public List<Course> findCoursesByPostalCodeAndDate(String postalCode, String dateFilter) {
        return findCoursesByPostalCodeAndDate(postalCode, dateFilter, 0, null, null);
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