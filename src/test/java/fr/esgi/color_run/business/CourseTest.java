package fr.esgi.color_run.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CourseTest {

    @Test
    @DisplayName("Test du constructeur par défaut")
    void testConstructeur() {
        // Act
        Course course = new Course();

        // Assert
        assertThat(course).isNotNull();
        assertThat(course.getId()).isNotNull();
        assertThat(course.getAssociationId()).isNull();
        assertThat(course.getMemberCreatorId()).isEqualTo(0);
        assertThat(course.getStartpositionLatitude()).isEqualTo(48.8566);
        assertThat(course.getStartpositionLongitude()).isEqualTo(2.3522);
        assertThat(course.getEndpositionLatitude()).isEqualTo(49.8566);
        assertThat(course.getEndpositionLongitude()).isEqualTo(2.9522);
        assertThat(course.getMaxOfRunners()).isEqualTo(100);
        assertThat(course.getCurrentNumberOfRunners()).isEqualTo(0);
        assertThat(course.getPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("Test des setters et getters")
    void testSettersGetters() {
        // Arrange
        Course course = new Course();
        Long id = 1L;
        String name = "Color Run Paris";
        String description = "Une course colorée dans Paris";
        Integer associationId = 2;
        Integer memberCreatorId = 3;
        LocalDateTime startDate = LocalDateTime.of(2023, 6, 15, 10, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 6, 15, 12, 0);
        double startLat = 48.8566;
        double startLong = 2.3522;
        double endLat = 48.8600;
        double endLong = 2.3700;
        Double distance = 5.0;
        String address = "Champ de Mars";
        String city = "Paris";
        Integer zipCode = 75007;
        Integer maxRunners = 500;
        Integer currentRunners = 250;
        double price = 25.0;

        // Act
        course.setId(id);
        course.setName(name);
        course.setDescription(description);
        course.setAssociationId(associationId);
        course.setMemberCreatorId(memberCreatorId);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setStartpositionLatitude(startLat);
        course.setStartpositionLongitude(startLong);
        course.setEndpositionLatitude(endLat);
        course.setEndpositionLongitude(endLong);
        course.setDistance(distance);
        course.setAddress(address);
        course.setCity(city);
        course.setZipCode(zipCode);
        course.setMaxOfRunners(maxRunners);
        course.setCurrentNumberOfRunners(currentRunners);
        course.setPrice(price);

        // Assert
        assertThat(course.getId()).isEqualTo(id);
        assertThat(course.getName()).isEqualTo(name);
        assertThat(course.getDescription()).isEqualTo(description);
        assertThat(course.getAssociationId()).isEqualTo(associationId);
        assertThat(course.getMemberCreatorId()).isEqualTo(memberCreatorId);
        assertThat(course.getStartDate()).isEqualTo(startDate);
        assertThat(course.getEndDate()).isEqualTo(endDate);
        assertThat(course.getStartpositionLatitude()).isEqualTo(startLat);
        assertThat(course.getStartpositionLongitude()).isEqualTo(startLong);
        assertThat(course.getEndpositionLatitude()).isEqualTo(endLat);
        assertThat(course.getEndpositionLongitude()).isEqualTo(endLong);
        assertThat(course.getDistance()).isEqualTo(distance);
        assertThat(course.getAddress()).isEqualTo(address);
        assertThat(course.getCity()).isEqualTo(city);
        assertThat(course.getZipCode()).isEqualTo(zipCode);
        assertThat(course.getMaxOfRunners()).isEqualTo(maxRunners);
        assertThat(course.getCurrentNumberOfRunners()).isEqualTo(currentRunners);
        assertThat(course.getPrice()).isEqualTo(price);
    }

    @Test
    @DisplayName("Test de getFormattedStartDate avec date non nulle")
    void testGetFormattedStartDate_WithNonNullDate() {
        // Arrange
        Course course = new Course();
        LocalDateTime date = LocalDateTime.of(2023, 6, 15, 10, 30);
        course.setStartDate(date);

        // Act
        String result = course.getFormattedStartDate();

        // Assert
        assertThat(result).isEqualTo("15/06/2023 à 10:30");
    }

    @Test
    @DisplayName("Test de getFormattedStartDate avec date nulle")
    void testGetFormattedStartDate_WithNullDate() {
        // Arrange
        Course course = new Course();
        course.setStartDate(null);

        // Act
        String result = course.getFormattedStartDate();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Test de getFormattedEndDate avec date non nulle")
    void testGetFormattedEndDate_WithNonNullDate() {
        // Arrange
        Course course = new Course();
        LocalDateTime date = LocalDateTime.of(2023, 6, 15, 12, 45);
        course.setEndDate(date);

        // Act
        String result = course.getFormattedEndDate();

        // Assert
        assertThat(result).isEqualTo("15/06/2023 à 12h45");
    }

    @Test
    @DisplayName("Test de getFormattedEndDate avec date nulle")
    void testGetFormattedEndDate_WithNullDate() {
        // Arrange
        Course course = new Course();
        course.setEndDate(null);

        // Act
        String result = course.getFormattedEndDate();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Test de getFormattedDate avec date non nulle")
    void testGetFormattedDate_WithNonNullDate() {
        // Arrange
        Course course = new Course();
        LocalDateTime date = LocalDateTime.of(2023, 6, 15, 10, 0);
        course.setStartDate(date);

        // Act
        String result = course.getFormattedDate();

        // Assert
        assertThat(result).isEqualTo("15/06/2023");
    }

    @Test
    @DisplayName("Test de getFormattedDate avec date nulle")
    void testGetFormattedDate_WithNullDate() {
        // Arrange
        Course course = new Course();
        course.setStartDate(null);

        // Act
        String result = course.getFormattedDate();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Test de getFormattedTime avec date non nulle")
    void testGetFormattedTime_WithNonNullDate() {
        // Arrange
        Course course = new Course();
        LocalDateTime date = LocalDateTime.of(2023, 6, 15, 14, 30);
        course.setStartDate(date);

        // Act
        String result = course.getFormattedTime();

        // Assert
        assertThat(result).isEqualTo("14h30");
    }

    @Test
    @DisplayName("Test de getFormattedTime avec date nulle")
    void testGetFormattedTime_WithNullDate() {
        // Arrange
        Course course = new Course();
        course.setStartDate(null);

        // Act
        String result = course.getFormattedTime();

        // Assert
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "1, january",
        "2, february",
        "3, march",
        "4, april",
        "5, may",
        "6, june",
        "7, july",
        "8, august",
        "9, september",
        "10, october",
        "11, november",
        "12, december"
    })
    @DisplayName("Test de getMonthName pour chaque mois")
    void testGetMonthName_ForAllMonths(int month, String expectedName) {
        // Arrange
        Course course = new Course();
        LocalDateTime date = LocalDateTime.of(2023, month, 15, 10, 0);
        course.setStartDate(date);

        // Act
        String result = course.getMonthName();

        // Assert
        assertThat(result).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Test de getMonthName avec date nulle")
    void testGetMonthName_WithNullDate() {
        // Arrange
        Course course = new Course();
        course.setStartDate(null);

        // Act
        String result = course.getMonthName();

        // Assert
        assertThat(result).isEmpty();
    }
}
