package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.GeoLocation;
import fr.esgi.color_run.service.GeocodingService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

class GeocodingServiceImplTest {

    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingServiceImpl();
    }

    @Test
    @DisplayName("Test de calcul de distance entre Paris et Lyon")
    void testCalculateDistance_ParisLyon() {
        // Arrange
        double parisLat = 48.8566;
        double parisLon = 2.3522;
        double lyonLat = 45.764;
        double lyonLon = 4.8357;
        double distanceAttendue = 392.0; // km approximatifs

        // Act
        double distanceCalculee = geocodingService.calculateDistance(parisLat, parisLon, lyonLat, lyonLon);

        // Assert
        assertThat(distanceCalculee).isCloseTo(distanceAttendue, withinPercentage(10.0)); // Tolérance de 10%
    }

    @Test
    @DisplayName("Test de calcul de distance entre Paris et Marseille")
    void testCalculateDistance_ParisMarseille() {
        // Arrange
        double parisLat = 48.8566;
        double parisLon = 2.3522;
        double marseilleLat = 43.296;
        double marseilleLon = 5.3697;
        double distanceAttendue = 660.0; // km approximatifs

        // Act
        double distanceCalculee = geocodingService.calculateDistance(parisLat, parisLon, marseilleLat, marseilleLon);

        // Assert
        assertThat(distanceCalculee).isCloseTo(distanceAttendue, withinPercentage(10.0)); // Tolérance de 10%
    }

    @Test
    @DisplayName("Test de calcul de distance pour deux points identiques")
    void testCalculateDistance_PointsIdentiques() {
        // Arrange
        double latitude = 48.8566;
        double longitude = 2.3522;

        // Act
        double distance = geocodingService.calculateDistance(latitude, longitude, latitude, longitude);

        // Assert
        assertThat(distance).isCloseTo(0.0, withinPercentage(0.1));
    }

    @ParameterizedTest
    @CsvSource({
            "48.8566, 2.3522, 45.764, 4.8357, 392.0", // Paris -> Lyon
            "48.8566, 2.3522, 43.296, 5.3697, 660.0", // Paris -> Marseille
            "45.764, 4.8357, 43.296, 5.3697, 275.0",  // Lyon -> Marseille
            "48.8566, 2.3522, 48.8566, 2.3522, 0.0"   // Même point
    })
    @DisplayName("Test de calcul de distance entre différents points")
    void testCalculateDistance_EntrePointsConnus(double lat1, double lon1, double lat2, double lon2, double distanceAttendue) {
        // Arrange
        // (paramètres fournis par CsvSource)

        // Act
        double distanceCalculee = geocodingService.calculateDistance(lat1, lon1, lat2, lon2);

        // Assert
        if (distanceAttendue == 0.0) {
            assertThat(distanceCalculee).isCloseTo(0.0, withinPercentage(0.1));
        } else {
            assertThat(distanceCalculee).isCloseTo(distanceAttendue, withinPercentage(10.0)); // Tolérance de 10%
        }
    }

    @Test
    @DisplayName("Test de récupération des coordonnées - retourne toujours une valeur")
    void testGetCoordinatesFromPostalCode_RetourneToujoursUneValeur() {
        // Arrange
        String codePostal = "75001";

        // Act
        GeoLocation resultat = geocodingService.getCoordinatesFromPostalCode(codePostal);

        // Assert
        assertThat(resultat).isNotNull();
        assertThat(resultat.getLatitude()).isNotNull();
        assertThat(resultat.getLongitude()).isNotNull();
        // Les coordonnées doivent être dans les limites raisonnables pour la France
        assertThat(resultat.getLatitude()).isBetween(40.0, 55.0);
        assertThat(resultat.getLongitude()).isBetween(-10.0, 15.0);
    }

    @Test
    @DisplayName("Test avec code postal null")
    void testGetCoordinatesFromPostalCode_AvecCodePostalNull() {
        // Arrange
        String codePostalNull = null;

        // Act
        GeoLocation resultat = geocodingService.getCoordinatesFromPostalCode(codePostalNull);

        // Assert
        assertThat(resultat).isNotNull();
        assertThat(resultat.getLatitude()).isNotNull();
        assertThat(resultat.getLongitude()).isNotNull();
        // Doit retourner des coordonnées par défaut (probablement Paris)
        assertThat(resultat.getLatitude()).isBetween(40.0, 55.0);
        assertThat(resultat.getLongitude()).isBetween(-10.0, 15.0);
    }

    @Test
    @DisplayName("Test avec code postal vide")
    void testGetCoordinatesFromPostalCode_AvecCodePostalVide() {
        // Arrange
        String codePostalVide = "";

        // Act
        GeoLocation resultat = geocodingService.getCoordinatesFromPostalCode(codePostalVide);

        // Assert
        assertThat(resultat).isNotNull();
        assertThat(resultat.getLatitude()).isNotNull();
        assertThat(resultat.getLongitude()).isNotNull();
        // Doit retourner des coordonnées par défaut
        assertThat(resultat.getLatitude()).isBetween(40.0, 55.0);
        assertThat(resultat.getLongitude()).isBetween(-10.0, 15.0);
    }

    @Test
    @DisplayName("Test de cache - deux appels successifs avec le même code postal")
    void testGetCoordinatesFromPostalCode_UtilisationDuCache() {
        // Arrange
        String codePostal = "75001";

        // Act - Premier appel
        GeoLocation premierResultat = geocodingService.getCoordinatesFromPostalCode(codePostal);

        // Act - Deuxième appel (devrait utiliser le cache)
        GeoLocation deuxiemeResultat = geocodingService.getCoordinatesFromPostalCode(codePostal);

        // Assert
        assertThat(premierResultat).isNotNull();
        assertThat(deuxiemeResultat).isNotNull();
        assertThat(premierResultat.getLatitude()).isEqualTo(deuxiemeResultat.getLatitude());
        assertThat(premierResultat.getLongitude()).isEqualTo(deuxiemeResultat.getLongitude());
    }

    @ParameterizedTest
    @CsvSource({
            "75001", // Paris 1er
            "69000", // Lyon
            "13000", // Marseille
            "33000", // Bordeaux
            "59000"  // Lille
    })
    @DisplayName("Test avec différents codes postaux français valides")
    void testGetCoordinatesFromPostalCode_AvecDifferentsCodesPostaux(String codePostal) {
        // Arrange
        // (code postal fourni par CsvSource)

        // Act
        GeoLocation resultat = geocodingService.getCoordinatesFromPostalCode(codePostal);

        // Assert
        assertThat(resultat).isNotNull();
        assertThat(resultat.getLatitude()).isNotNull();
        assertThat(resultat.getLongitude()).isNotNull();
        // Les coordonnées doivent être dans les limites de la France
        assertThat(resultat.getLatitude()).isBetween(40.0, 55.0);
        assertThat(resultat.getLongitude()).isBetween(-10.0, 15.0);
    }

    @Test
    @DisplayName("Test de calcul de distance avec coordonnées aux limites")
    void testCalculateDistance_CoordonneesLimites() {
        // Arrange
        double lat1 = 90.0;  // Pôle Nord
        double lon1 = 0.0;
        double lat2 = -90.0; // Pôle Sud
        double lon2 = 0.0;

        // Act
        double distance = geocodingService.calculateDistance(lat1, lon1, lat2, lon2);

        // Assert
        // La distance entre les pôles doit être d'environ 20 000 km (la moitié de la circonférence terrestre)
        assertThat(distance).isCloseTo(20003.93, withinPercentage(5.0));
    }

    @Test
    @DisplayName("Test de calcul de distance avec coordonnées équateur")
    void testCalculateDistance_CoordinatesEquateur() {
        // Arrange
        double lat1 = 0.0;   // Équateur
        double lon1 = 0.0;   // Greenwich
        double lat2 = 0.0;   // Équateur
        double lon2 = 180.0; // Antiméridien

        // Act
        double distance = geocodingService.calculateDistance(lat1, lon1, lat2, lon2);

        // Assert
        // La distance à l'équateur sur la moitié de la circonférence doit être d'environ 20 000 km
        assertThat(distance).isCloseTo(20003.93, withinPercentage(5.0));
    }
}