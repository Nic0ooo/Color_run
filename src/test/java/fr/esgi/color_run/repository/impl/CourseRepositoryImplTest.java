package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.util.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRepositoryImplTest {

    @Mock
    private GeocodingService geocodingService;

    private CourseRepositoryImpl courseRepository;

    @BeforeEach
    void setUp() {
        // Activer le mode test pour la base de données
        DatabaseManager.enableTestMode();

        // Créer le repository avec le service mocké
        courseRepository = new CourseRepositoryImpl(geocodingService);

        // Nettoyer la base de test avant chaque test
        cleanDatabase();
    }

    /**
     * Nettoie la base de données de test
     */
    private void cleanDatabase() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // Désactiver les contraintes temporairement pour nettoyer
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("DELETE FROM coursemember");
            stmt.execute("DELETE FROM course");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

        } catch (SQLException e) {
            System.err.println("Erreur lors du nettoyage de la base : " + e.getMessage());
        }
    }

    /**
     * Crée une course de test complète
     */
    private Course createTestCourse(String name, String city, LocalDateTime startDate) {
        Course course = new Course();
        course.setName(name);
        course.setDescription("Description de " + name);
        course.setCity(city);
        course.setZipCode(75001);
        course.setStartDate(startDate);
        course.setEndDate(startDate.plusHours(2));
        course.setMemberCreatorId(1);
        course.setStartpositionLatitude(48.8566);
        course.setStartpositionLongitude(2.3522);
        course.setEndpositionLatitude(48.8600);
        course.setEndpositionLongitude(2.3700);
        course.setDistance(5.0); // Important : définir une distance non-null
        course.setAddress("123 Rue de Test");
        course.setMaxOfRunners(100);
        course.setCurrentNumberOfRunners(0);
        course.setPrice(25.0);
        return course;
    }

    @Test
    @DisplayName("save - devrait enregistrer une nouvelle course avec succès")
    void save_shouldInsertCourseSuccessfully() {
        // Arrange
        Course newCourse = createTestCourse("Course Test Save", "Paris", LocalDateTime.now().plusDays(10));

        // Act
        Course savedCourse = courseRepository.save(newCourse);

        // Assert
        assertThat(savedCourse).isNotNull();
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getName()).isEqualTo("Course Test Save");
        assertThat(savedCourse.getCity()).isEqualTo("Paris");

        // Vérifier que la course est bien en base
        Course foundCourse = courseRepository.findById(savedCourse.getId());
        assertThat(foundCourse).isNotNull();
        assertThat(foundCourse.getName()).isEqualTo("Course Test Save");
    }

    @Test
    @DisplayName("findAll - devrait retourner toutes les courses")
    void findAll_shouldReturnAllCourses() {
        // Arrange
        Course course1 = createTestCourse("Course 1", "Paris", LocalDateTime.now().plusDays(10));
        Course course2 = createTestCourse("Course 2", "Lyon", LocalDateTime.now().minusDays(5));
        Course course3 = createTestCourse("Course 3", "Marseille", LocalDateTime.now().plusDays(20));

        courseRepository.save(course1);
        courseRepository.save(course2);
        courseRepository.save(course3);

        // Act
        List<Course> courses = courseRepository.findAll();

        // Assert
        assertThat(courses).isNotNull();
        assertThat(courses).hasSize(3);
        assertThat(courses).extracting("name")
                .containsExactlyInAnyOrder("Course 1", "Course 2", "Course 3");
    }

    @Test
    @DisplayName("findAll - devrait retourner une liste vide si aucune course")
    void findAll_shouldReturnEmptyList_whenNoCourses() {
        // Act
        List<Course> courses = courseRepository.findAll();

        // Assert
        assertThat(courses).isNotNull();
        assertThat(courses).isEmpty();
    }

    @Test
    @DisplayName("findById - devrait retourner la course correspondante")
    void findById_shouldReturnCourseWithMatchingId() {
        // Arrange
        Course savedCourse = courseRepository.save(
                createTestCourse("Course FindById", "Toulouse", LocalDateTime.now().plusDays(15))
        );

        // Act
        Course foundCourse = courseRepository.findById(savedCourse.getId());

        // Assert
        assertThat(foundCourse).isNotNull();
        assertThat(foundCourse.getId()).isEqualTo(savedCourse.getId());
        assertThat(foundCourse.getName()).isEqualTo("Course FindById");
        assertThat(foundCourse.getCity()).isEqualTo("Toulouse");
    }

    @Test
    @DisplayName("findById - devrait retourner null si ID inexistant")
    void findById_shouldReturnNull_whenIdDoesNotExist() {
        // Act
        Course course = courseRepository.findById(999L);

        // Assert
        assertThat(course).isNull();
    }

    @Test
    @DisplayName("findUpcomingCourses - devrait retourner seulement les courses à venir")
    void findUpcomingCourses_shouldReturnOnlyUpcomingCourses() {
        // Arrange
        Course pastCourse = createTestCourse("Course Passée", "Nice", LocalDateTime.now().minusDays(10));
        Course futureCourse1 = createTestCourse("Course Future 1", "Bordeaux", LocalDateTime.now().plusDays(10));
        Course futureCourse2 = createTestCourse("Course Future 2", "Lille", LocalDateTime.now().plusDays(20));

        courseRepository.save(pastCourse);
        courseRepository.save(futureCourse1);
        courseRepository.save(futureCourse2);

        // Act
        List<Course> upcomingCourses = courseRepository.findUpcomingCourses();

        // Assert
        assertThat(upcomingCourses).isNotNull();
        assertThat(upcomingCourses).hasSize(2);
        assertThat(upcomingCourses).extracting("name")
                .containsExactlyInAnyOrder("Course Future 1", "Course Future 2");
    }

    @Test
    @DisplayName("findPastCourses - devrait retourner seulement les courses passées")
    void findPastCourses_shouldReturnOnlyPastCourses() {
        // Arrange
        Course pastCourse1 = createTestCourse("Course Passée 1", "Strasbourg", LocalDateTime.now().minusDays(10));
        Course pastCourse2 = createTestCourse("Course Passée 2", "Nantes", LocalDateTime.now().minusDays(5));
        Course futureCourse = createTestCourse("Course Future", "Rennes", LocalDateTime.now().plusDays(10));

        courseRepository.save(pastCourse1);
        courseRepository.save(pastCourse2);
        courseRepository.save(futureCourse);

        // Act
        List<Course> pastCourses = courseRepository.findPastCourses();

        // Assert
        assertThat(pastCourses).isNotNull();
        assertThat(pastCourses).hasSize(2);
        assertThat(pastCourses).extracting("name")
                .containsExactlyInAnyOrder("Course Passée 1", "Course Passée 2");
    }

    @Test
    @DisplayName("searchCourseByName - devrait retourner les courses contenant le terme recherché")
    void searchCourseByName_shouldReturnCoursesContainingSearchTerm() {
        // Arrange
        Course course1 = createTestCourse("Marathon de Paris", "Paris", LocalDateTime.now().plusDays(10));
        Course course2 = createTestCourse("Color Run Lyon", "Lyon", LocalDateTime.now().plusDays(15));
        Course course3 = createTestCourse("Marathon de Marseille", "Marseille", LocalDateTime.now().plusDays(20));

        courseRepository.save(course1);
        courseRepository.save(course2);
        courseRepository.save(course3);

        // Act
        List<Course> marathonCourses = courseRepository.searchCourseByName("Marathon");
        List<Course> colorCourses = courseRepository.searchCourseByName("Color");

        // Assert
        assertThat(marathonCourses).hasSize(2);
        assertThat(marathonCourses).extracting("name")
                .containsExactlyInAnyOrder("Marathon de Paris", "Marathon de Marseille");

        assertThat(colorCourses).hasSize(1);
        assertThat(colorCourses.get(0).getName()).isEqualTo("Color Run Lyon");
    }

    @Test
    @DisplayName("updateCourse - devrait mettre à jour une course existante")
    void updateCourse_shouldUpdateExistingCourse() {
        // Arrange
        Course originalCourse = courseRepository.save(
                createTestCourse("Course Originale", "Montpellier", LocalDateTime.now().plusDays(10))
        );

        // Modifier la course
        originalCourse.setName("Course Modifiée");
        originalCourse.setDescription("Description modifiée");
        originalCourse.setCity("Avignon");
        originalCourse.setPrice(30.0);

        // Act
        Course updatedCourse = courseRepository.updateCourse(originalCourse);

        // Assert
        assertThat(updatedCourse).isNotNull();
        assertThat(updatedCourse.getName()).isEqualTo("Course Modifiée");
        assertThat(updatedCourse.getDescription()).isEqualTo("Description modifiée");
        assertThat(updatedCourse.getCity()).isEqualTo("Avignon");
        assertThat(updatedCourse.getPrice()).isEqualTo(30.0);

        // Vérifier en base
        Course courseFromDb = courseRepository.findById(originalCourse.getId());
        assertThat(courseFromDb.getName()).isEqualTo("Course Modifiée");
    }

    @Test
    @DisplayName("updateCourse - devrait retourner null si ID est null")
    void updateCourse_shouldReturnNull_whenIdIsNull() {
        // Arrange
        Course courseWithNullId = createTestCourse("Course Sans ID", "Dijon", LocalDateTime.now().plusDays(10));
        courseWithNullId.setId(null);

        // Act
        Course result = courseRepository.updateCourse(courseWithNullId);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("deleteCourse - devrait supprimer une course existante")
    void deleteCourse_shouldDeleteExistingCourse() {
        // Arrange
        Course savedCourse = courseRepository.save(
                createTestCourse("Course à Supprimer", "Clermont-Ferrand", LocalDateTime.now().plusDays(10))
        );
        Long courseId = savedCourse.getId();

        // Vérifier que la course existe
        assertThat(courseRepository.findById(courseId)).isNotNull();

        // Act
        courseRepository.deleteCourse(courseId);

        // Assert
        assertThat(courseRepository.findById(courseId)).isNull();
    }

    @Test
    @DisplayName("deleteCourse - devrait lancer une exception si ID inexistant")
    void deleteCourse_shouldThrowException_whenIdDoesNotExist() {
        // Act & Assert
        assertThatThrownBy(() -> courseRepository.deleteCourse(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Impossible de supprimer la course");
    }

    @Test
    @DisplayName("findByProximity - devrait retourner les courses dans le rayon spécifié")
    void findByProximity_shouldReturnCoursesWithinRadius() {
        // Arrange
        Course course1 = createTestCourse("Course Proche", "Paris", LocalDateTime.now().plusDays(10));
        course1.setStartpositionLatitude(48.8566);
        course1.setStartpositionLongitude(2.3522);

        Course course2 = createTestCourse("Course Lointaine", "Marseille", LocalDateTime.now().plusDays(15));
        course2.setStartpositionLatitude(43.2965);
        course2.setStartpositionLongitude(5.3698);

        courseRepository.save(course1);
        courseRepository.save(course2);

        // Configurer le mock du service de géocodage
        when(geocodingService.calculateDistance(eq(48.8566), eq(2.3522), eq(48.8566), eq(2.3522)))
                .thenReturn(0.0); // Distance nulle pour la course 1
        when(geocodingService.calculateDistance(eq(48.8566), eq(2.3522), eq(43.2965), eq(5.3698)))
                .thenReturn(500.0); // Distance importante pour la course 2

        // Act
        List<Course> coursesInRadius = courseRepository.findByProximity(48.8566, 2.3522, 100);

        // Assert
        assertThat(coursesInRadius).hasSize(1);
        assertThat(coursesInRadius.get(0).getName()).isEqualTo("Course Proche");

        // Vérifier les appels au service de géocodage
        verify(geocodingService, times(2)).calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("findByPostalCode - devrait retourner les courses du code postal spécifié")
    void findByPostalCode_shouldReturnCoursesWithMatchingPostalCode() {
        // Arrange
        Course course75 = createTestCourse("Course Paris", "Paris", LocalDateTime.now().plusDays(10));
        course75.setZipCode(75001);

        Course course69 = createTestCourse("Course Lyon", "Lyon", LocalDateTime.now().plusDays(15));
        course69.setZipCode(69001);

        Course course13 = createTestCourse("Course Marseille", "Marseille", LocalDateTime.now().plusDays(20));
        course13.setZipCode(13001);

        courseRepository.save(course75);
        courseRepository.save(course69);
        courseRepository.save(course13);

        // Act
        List<Course> coursesParis = courseRepository.findByPostalCode("75001");
        List<Course> coursesLyon = courseRepository.findByPostalCode("69001");

        // Assert
        assertThat(coursesParis).hasSize(1);
        assertThat(coursesParis.get(0).getName()).isEqualTo("Course Paris");

        assertThat(coursesLyon).hasSize(1);
        assertThat(coursesLyon.get(0).getName()).isEqualTo("Course Lyon");
    }

    @Test
    @DisplayName("findByPostalCode - devrait retourner toutes les courses si code postal vide")
    void findByPostalCode_shouldReturnAllCourses_whenPostalCodeIsEmpty() {
        // Arrange
        courseRepository.save(createTestCourse("Course 1", "Paris", LocalDateTime.now().plusDays(10)));
        courseRepository.save(createTestCourse("Course 2", "Lyon", LocalDateTime.now().plusDays(15)));

        // Act
        List<Course> allCourses = courseRepository.findByPostalCode("");
        List<Course> allCoursesNull = courseRepository.findByPostalCode(null);

        // Assert
        assertThat(allCourses).hasSize(2);
        assertThat(allCoursesNull).hasSize(2);
    }

    @Test
    @DisplayName("searchAndSortCourses - devrait filtrer et trier correctement")
    void searchAndSortCourses_shouldFilterAndSortCorrectly() {
        // Arrange
        LocalDateTime baseDate = LocalDateTime.now();

        Course course1 = createTestCourse("Alpha Course", "Paris", baseDate.plusDays(10));
        Course course2 = createTestCourse("Beta Course", "Lyon", baseDate.plusDays(5));
        Course course3 = createTestCourse("Gamma Course", "Marseille", baseDate.plusDays(15));

        courseRepository.save(course1);
        courseRepository.save(course2);
        courseRepository.save(course3);

        // Act - Recherche avec tri par nom croissant
        List<Course> sortedByName = courseRepository.searchAndSortCourses(
                null, null, null, "name", "asc", true
        );

        // Act - Recherche avec terme "Course" et tri par date
        List<Course> filteredCourses = courseRepository.searchAndSortCourses(
                "Course", null, null, "startdate", "asc", true
        );

        // Assert
        assertThat(sortedByName).hasSize(3);
        assertThat(sortedByName.get(0).getName()).isEqualTo("Alpha Course");
        assertThat(sortedByName.get(1).getName()).isEqualTo("Beta Course");
        assertThat(sortedByName.get(2).getName()).isEqualTo("Gamma Course");

        assertThat(filteredCourses).hasSize(3);
        // Tri par date : Beta (J+5), Alpha (J+10), Gamma (J+15)
        assertThat(filteredCourses.get(0).getName()).isEqualTo("Beta Course");
        assertThat(filteredCourses.get(1).getName()).isEqualTo("Alpha Course");
        assertThat(filteredCourses.get(2).getName()).isEqualTo("Gamma Course");
    }

    @Test
    @DisplayName("findByMonth - devrait retourner les courses du mois spécifié")
    void findByMonth_shouldReturnCoursesInSpecifiedMonth() {
        // Arrange
        // Créer des courses dans différents mois
        Course courseJanvier = createTestCourse("Course Janvier", "Paris",
                LocalDateTime.of(2024, 1, 15, 10, 0));
        Course courseFevrier = createTestCourse("Course Février", "Lyon",
                LocalDateTime.of(2024, 2, 20, 14, 0));
        Course courseJanvier2 = createTestCourse("Course Janvier 2", "Marseille",
                LocalDateTime.of(2024, 1, 25, 9, 0));

        courseRepository.save(courseJanvier);
        courseRepository.save(courseFevrier);
        courseRepository.save(courseJanvier2);

        // Act
        List<Course> coursesJanvier = courseRepository.findByMonth("01");
        List<Course> coursesFevrier = courseRepository.findByMonth("02");
        List<Course> coursesMars = courseRepository.findByMonth("03");

        // Assert
        assertThat(coursesJanvier).hasSize(2);
        assertThat(coursesJanvier).extracting("name")
                .containsExactlyInAnyOrder("Course Janvier", "Course Janvier 2");

        assertThat(coursesFevrier).hasSize(1);
        assertThat(coursesFevrier.get(0).getName()).isEqualTo("Course Février");

        assertThat(coursesMars).isEmpty();
    }

    @Test
    @DisplayName("findPastCoursesByAssociationId - devrait retourner les courses passées d'une association")
    void findPastCoursesByAssociationId_shouldReturnPastCoursesForAssociation() {
        // Arrange
        Course pastCourse1 = createTestCourse("Course Passée Asso 1", "Paris", LocalDateTime.now().minusDays(10));
        pastCourse1.setAssociationId(1);

        Course pastCourse2 = createTestCourse("Course Passée Asso 1 bis", "Lyon", LocalDateTime.now().minusDays(5));
        pastCourse2.setAssociationId(1);

        Course pastCourse3 = createTestCourse("Course Passée Asso 2", "Marseille", LocalDateTime.now().minusDays(15));
        pastCourse3.setAssociationId(2);

        Course futureCourse = createTestCourse("Course Future Asso 1", "Nice", LocalDateTime.now().plusDays(10));
        futureCourse.setAssociationId(1);

        courseRepository.save(pastCourse1);
        courseRepository.save(pastCourse2);
        courseRepository.save(pastCourse3);
        courseRepository.save(futureCourse);

        // Act
        List<Course> pastCoursesAsso1 = courseRepository.findPastCoursesByAssociationId(1L);
        List<Course> pastCoursesAsso2 = courseRepository.findPastCoursesByAssociationId(2L);
        List<Course> pastCoursesAsso3 = courseRepository.findPastCoursesByAssociationId(3L);

        // Assert
        assertThat(pastCoursesAsso1).hasSize(2);
        assertThat(pastCoursesAsso1).extracting("name")
                .containsExactlyInAnyOrder("Course Passée Asso 1", "Course Passée Asso 1 bis");

        assertThat(pastCoursesAsso2).hasSize(1);
        assertThat(pastCoursesAsso2.get(0).getName()).isEqualTo("Course Passée Asso 2");

        assertThat(pastCoursesAsso3).isEmpty();
    }

    @Test
    @DisplayName("findUpcomingCoursesByAssociationId - devrait retourner les courses à venir d'une association")
    void findUpcomingCoursesByAssociationId_shouldReturnUpcomingCoursesForAssociation() {
        // Arrange
        Course futureCourse1 = createTestCourse("Course Future Asso 1", "Paris", LocalDateTime.now().plusDays(10));
        futureCourse1.setAssociationId(1);

        Course futureCourse2 = createTestCourse("Course Future Asso 1 bis", "Lyon", LocalDateTime.now().plusDays(15));
        futureCourse2.setAssociationId(1);

        Course futureCourse3 = createTestCourse("Course Future Asso 2", "Marseille", LocalDateTime.now().plusDays(20));
        futureCourse3.setAssociationId(2);

        Course pastCourse = createTestCourse("Course Passée Asso 1", "Nice", LocalDateTime.now().minusDays(10));
        pastCourse.setAssociationId(1);

        courseRepository.save(futureCourse1);
        courseRepository.save(futureCourse2);
        courseRepository.save(futureCourse3);
        courseRepository.save(pastCourse);

        // Act
        List<Course> futureCoursesAsso1 = courseRepository.findUpcomingCoursesByAssociationId(1L);
        List<Course> futureCoursesAsso2 = courseRepository.findUpcomingCoursesByAssociationId(2L);
        List<Course> futureCoursesAsso3 = courseRepository.findUpcomingCoursesByAssociationId(3L);

        // Assert
        assertThat(futureCoursesAsso1).hasSize(2);
        assertThat(futureCoursesAsso1).extracting("name")
                .containsExactlyInAnyOrder("Course Future Asso 1", "Course Future Asso 1 bis");

        assertThat(futureCoursesAsso2).hasSize(1);
        assertThat(futureCoursesAsso2.get(0).getName()).isEqualTo("Course Future Asso 2");

        assertThat(futureCoursesAsso3).isEmpty();
    }

    @Test
    @DisplayName("findPastCoursesByAssociationId - devrait retourner liste vide si associationId est null")
    void findPastCoursesByAssociationId_shouldReturnEmptyList_whenAssociationIdIsNull() {
        // Act
        List<Course> result = courseRepository.findPastCoursesByAssociationId(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findUpcomingCoursesByAssociationId - devrait retourner liste vide si associationId est null")
    void findUpcomingCoursesByAssociationId_shouldReturnEmptyList_whenAssociationIdIsNull() {
        // Act
        List<Course> result = courseRepository.findUpcomingCoursesByAssociationId(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}