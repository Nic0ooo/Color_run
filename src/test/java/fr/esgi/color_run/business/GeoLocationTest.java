package fr.esgi.color_run.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class GeoLocationTest {

    @Test
    @DisplayName("Test du constructeur avec coordonnées valides")
    void testConstructeurAvecCoordonneesValides() {
        // Arrange
        double latitude = 48.8566;
        double longitude = 2.3522;

        // Act
        GeoLocation geoLocation = new GeoLocation(latitude, longitude);

        // Assert
        assertThat(geoLocation).isNotNull();
        assertThat(geoLocation.getLatitude()).isEqualTo(latitude);
        assertThat(geoLocation.getLongitude()).isEqualTo(longitude);
    }

    @ParameterizedTest
    @CsvSource({
            "41.0, -5.0",    // Sud-Ouest France
            "51.5, 10.0",    // Nord-Est France
            "48.8566, 2.3522", // Paris
            "45.764, 4.8357",  // Lyon
            "43.296, 5.3697"   // Marseille
    })
    @DisplayName("Test du constructeur avec coordonnées limites valides pour la France")
    void testConstructeurAvecCoordonneesLimitesValides(double latitude, double longitude) {
        // Arrange & Act & Assert
        assertThatCode(() -> new GeoLocation(latitude, longitude))
                .doesNotThrowAnyException();

        GeoLocation geoLocation = new GeoLocation(latitude, longitude);
        assertThat(geoLocation.getLatitude()).isEqualTo(latitude);
        assertThat(geoLocation.getLongitude()).isEqualTo(longitude);
    }

    @Test
    @DisplayName("Test de setLatitude")
    void testSetLatitude() {
        // Arrange
        GeoLocation geoLocation = new GeoLocation(48.8566, 2.3522);
        double nouvelleLatitude = 45.764;

        // Act
        geoLocation.setLatitude(nouvelleLatitude);

        // Assert
        assertThat(geoLocation.getLatitude()).isEqualTo(nouvelleLatitude);
        assertThat(geoLocation.getLongitude()).isEqualTo(2.3522); // Vérifier que longitude n'a pas changé
    }

    @Test
    @DisplayName("Test de setLongitude")
    void testSetLongitude() {
        // Arrange
        GeoLocation geoLocation = new GeoLocation(48.8566, 2.3522);
        double nouvelleLongitude = 4.8357;

        // Act
        geoLocation.setLongitude(nouvelleLongitude);

        // Assert
        assertThat(geoLocation.getLongitude()).isEqualTo(nouvelleLongitude);
        assertThat(geoLocation.getLatitude()).isEqualTo(48.8566); // Vérifier que latitude n'a pas changé
    }

    @Test
    @DisplayName("Test de getLatitude")
    void testGetLatitude() {
        // Arrange
        double latitude = 43.296;
        GeoLocation geoLocation = new GeoLocation(latitude, 5.3697);

        // Act
        double resultat = geoLocation.getLatitude();

        // Assert
        assertThat(resultat).isEqualTo(latitude);
    }

    @Test
    @DisplayName("Test de getLongitude")
    void testGetLongitude() {
        // Arrange
        double longitude = 5.3697;
        GeoLocation geoLocation = new GeoLocation(43.296, longitude);

        // Act
        double resultat = geoLocation.getLongitude();

        // Assert
        assertThat(resultat).isEqualTo(longitude);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0",          // Équateur/Greenwich
            "-90.0, -180.0",     // Pôle Sud/Limite Ouest
            "90.0, 180.0",       // Pôle Nord/Limite Est
            "48.8566, 2.3522"    // Paris
    })
    @DisplayName("Test avec différentes coordonnées mondiales")
    void testAvecDifferentesCoordonneesMondiales(double latitude, double longitude) {
        // Arrange & Act
        GeoLocation geoLocation = new GeoLocation(latitude, longitude);

        // Assert
        assertThat(geoLocation.getLatitude()).isEqualTo(latitude);
        assertThat(geoLocation.getLongitude()).isEqualTo(longitude);
    }

    @Test
    @DisplayName("Test de modification successive des coordonnées")
    void testModificationSuccessiveDesCoordonnees() {
        // Arrange
        GeoLocation geoLocation = new GeoLocation(0.0, 0.0);
        double latitude1 = 48.8566;
        double longitude1 = 2.3522;
        double latitude2 = 45.764;
        double longitude2 = 4.8357;

        // Act & Assert - Première modification
        geoLocation.setLatitude(latitude1);
        geoLocation.setLongitude(longitude1);
        assertThat(geoLocation.getLatitude()).isEqualTo(latitude1);
        assertThat(geoLocation.getLongitude()).isEqualTo(longitude1);

        // Act & Assert - Deuxième modification
        geoLocation.setLatitude(latitude2);
        geoLocation.setLongitude(longitude2);
        assertThat(geoLocation.getLatitude()).isEqualTo(latitude2);
        assertThat(geoLocation.getLongitude()).isEqualTo(longitude2);
    }
}