package fr.esgi.color_run.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class Course_memberTest {

    @Test
    @DisplayName("Test du constructeur par défaut")
    void testConstructeur() {
        // Act
        Course_member course_member = new Course_member();

        // Assert
        assertThat(course_member).isNotNull();
        assertThat(course_member.getId()).isNotNull();
        assertThat(course_member.getCourseId()).isNull();
        assertThat(course_member.getMemberId()).isNull();
        assertThat(course_member.getRegistrationDate()).isNull();
        assertThat(course_member.getRegistrationStatus()).isEqualTo(Status.PENDING);
        assertThat(course_member.getStripeSessionId()).isNull();
        assertThat(course_member.getBibNumber()).isNull();
    }

    @Test
    @DisplayName("Test des setters et getters")
    void testSettersGetters() {
        // Arrange
        Course_member course_member = new Course_member();
        Long id = 1L;
        Long courseId = 2L;
        Long memberId = 3L;
        String registrationDate = "2023-06-15";
        Status registrationStatus = Status.ACCEPTED;
        String stripeSessionId = "cs_test_123456789";
        String bibNumber = "A123";

        // Act
        course_member.setId(id);
        course_member.setCourseId(courseId);
        course_member.setMemberId(memberId);
        course_member.setRegistrationDate(registrationDate);
        course_member.setRegistrationStatus(registrationStatus);
        course_member.setStripeSessionId(stripeSessionId);
        course_member.setBibNumber(bibNumber);

        // Assert
        assertThat(course_member.getId()).isEqualTo(id);
        assertThat(course_member.getCourseId()).isEqualTo(courseId);
        assertThat(course_member.getMemberId()).isEqualTo(memberId);
        assertThat(course_member.getRegistrationDate()).isEqualTo(registrationDate);
        assertThat(course_member.getRegistrationStatus()).isEqualTo(registrationStatus);
        assertThat(course_member.getStripeSessionId()).isEqualTo(stripeSessionId);
        assertThat(course_member.getBibNumber()).isEqualTo(bibNumber);
    }

    @Test
    @DisplayName("Test de hasBibNumber avec bibNumber non nul")
    void testHasBibNumber_WithNonNullBibNumber() {
        // Arrange
        Course_member course_member = new Course_member();
        course_member.setBibNumber("A123");

        // Act
        boolean result = course_member.hasBibNumber();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Test de hasBibNumber avec bibNumber nul")
    void testHasBibNumber_WithNullBibNumber() {
        // Arrange
        Course_member course_member = new Course_member();
        course_member.setBibNumber(null);

        // Act
        boolean result = course_member.hasBibNumber();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Test de hasBibNumber avec bibNumber vide")
    void testHasBibNumber_WithEmptyBibNumber() {
        // Arrange
        Course_member course_member = new Course_member();
        course_member.setBibNumber("");

        // Act
        boolean result = course_member.hasBibNumber();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Test de hasBibNumber avec bibNumber contenant uniquement des espaces")
    void testHasBibNumber_WithSpacesOnlyBibNumber() {
        // Arrange
        Course_member course_member = new Course_member();
        course_member.setBibNumber("   ");

        // Act
        boolean result = course_member.hasBibNumber();

        // Assert
        assertThat(result).isFalse();
    }
}
