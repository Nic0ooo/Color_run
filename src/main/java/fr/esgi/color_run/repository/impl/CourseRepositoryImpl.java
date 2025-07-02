// File: src/main/java/fr/esgi/color_run/repository/impl/CourseRepositoryImpl.java
package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.util.DatabaseManager;
import fr.esgi.color_run.util.Mapper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import fr.esgi.color_run.util.PostalCodeMapper;


public class CourseRepositoryImpl implements CourseRepository {

    private final DatabaseManager dbManager;
    private final GeocodingService geocodingService;

    public CourseRepositoryImpl(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
        this.dbManager = DatabaseManager.getInstance();
        ensureTableExists();
    }

    private Connection getConnection() throws SQLException {
        return dbManager.getConnection();
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS course (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(255)," +
                "description VARCHAR(255)," +
                "associationid INT," +
                "membercreatorid INT," +
                "startdate TIMESTAMP," +
                "enddate TIMESTAMP," +
                "startpositionlatitude DOUBLE," +
                "startpositionlongitude DOUBLE," +
                "endpositionlatitude DOUBLE," +
                "endpositionlongitude DOUBLE," +
                "distance DOUBLE," +
                "address VARCHAR(255)," +
                "city VARCHAR(255)," +
                "zipcode INT," +
                "maxofrunners INT," +
                "currentnumberofrunners INT," +
                "price DOUBLE" +
                ");";

        dbManager.ensureTableExists("course", sql);
    }

    @Override
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        System.out.println("CourseRepositoryImpl: findAll() - Exécution de la requête pour récupérer toutes les courses.");
        String sql = "SELECT * FROM course";

        try (Connection connection = getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                courses.add(Mapper.mapRowToCourse(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (courses.isEmpty()) {
            System.out.println("❌ Aucune course trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + courses.size() + " courses trouvées dans la base de données.");
        }
            return courses;
    }

    @Override
    public List<Course> findUpcomingCourses() {
        List<Course> upcomingCourses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE startdate > CURRENT_TIMESTAMP ORDER BY startdate ASC";
        System.out.println("CourseRepositoryImpl: findUpcomingCourses() - Exécution de la requête pour récupérer les courses à venir.");

        try (Connection connection = getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                upcomingCourses.add(Mapper.mapRowToCourse(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (upcomingCourses.isEmpty()) {
            System.out.println("❌ Aucune course à venir trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + upcomingCourses.size() + " courses à venir trouvées dans la base de données.");
        }
        return upcomingCourses;
    }

    @Override
    public List<Course> findPastCourses() {
        List<Course> pastCourses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE startdate < CURRENT_TIMESTAMP ORDER BY startdate DESC";
        System.out.println("CourseRepositoryImpl: findPastCourses() - Exécution de la requête pour récupérer les courses passées.");

        try (Connection connection = getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                pastCourses.add(Mapper.mapRowToCourse(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (pastCourses.isEmpty()) {
            System.out.println("❌ Aucune course passée trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + pastCourses.size() + " courses passées trouvées dans la base de données.");
        }
        return pastCourses;
    }

    @Override
    public List<Course> searchCourseByName(String name) {
        List<Course> searchedCourses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE name LIKE ?";

        System.out.println("CourseRepositoryImpl: searchCourseByName() - Exécution de la requête pour rechercher les courses par nom.");
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                searchedCourses.add(Mapper.mapRowToCourse(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (searchedCourses.isEmpty()) {
            System.out.println("❌ Aucune course avec pour nom :" + name + " trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + searchedCourses.size() + " courses avec pour nom :" + name + "  trouvées dans la base de données.");
        }
        return searchedCourses;
    }

    @Override
    public Course findById(Long id) {
        Course course = null;
        String sql = "SELECT * FROM course WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                course = Mapper.mapRowToCourse(resultSet);
                System.out.println("CourseRepositoryImpl: findById() - Course trouvée avec l'ID : " + id);
            } else {
                System.out.println("❌ Aucune course trouvée avec l'ID : " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }

    @Override
    public List<Course> searchAndSortCourses(String searchTerm, LocalDate fromDate, LocalDate toDate,
                                             String sortBy, String sortDirection, boolean upcoming) {
        List<Course> courses = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM course WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        // Filtre par période (upcoming/past)
        if (upcoming) {
            sql.append(" AND startdate > CURRENT_TIMESTAMP");
        } else {
            sql.append(" AND startdate < CURRENT_TIMESTAMP");
        }

        // Filtre par terme de recherche (nom, ville, code postal)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE LOWER(?) OR LOWER(city) LIKE LOWER(?) OR CAST(zipcode AS VARCHAR) LIKE ?)");
            String searchPattern = "%" + searchTerm.trim() + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        // Filtre par date de début
        if (fromDate != null) {
            sql.append(" AND CAST(startdate AS DATE) >= ?");
            parameters.add(Date.valueOf(fromDate));
        }

        // Filtre par date de fin
        if (toDate != null) {
            sql.append(" AND CAST(startdate AS DATE) <= ?");
            parameters.add(Date.valueOf(toDate));
        }

        // Tri
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            String direction = "desc".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";

            switch (sortBy.toLowerCase()) {
                case "name":
                    sql.append(" ORDER BY LOWER(name) ").append(direction);
                    break;
                case "startdate":
                    sql.append(" ORDER BY startdate ").append(direction);
                    break;
                case "city":
                    sql.append(" ORDER BY LOWER(city) ").append(direction);
                    break;
                case "distance":
                    sql.append(" ORDER BY distance ").append(direction);
                    break;
                default:
                    sql.append(" ORDER BY startdate ASC"); // Tri par défaut
            }
        } else {
            // Tri par défaut : date croissante pour upcoming, décroissante pour past
            sql.append(" ORDER BY startdate ").append(upcoming ? "ASC" : "DESC");
        }

        System.out.println("Requête SQL générée: " + sql.toString());
        System.out.println("Paramètres: " + parameters);

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            // Définir les paramètres
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    courses.add(Mapper.mapRowToCourse(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche et tri des courses:");
            e.printStackTrace();
        }

        return courses;
    }

    @Override
    public List<Course> findByProximity(double latitude, double longitude, int radiusInKm) {
        List<Course> allCourses = findAll();

        return allCourses.stream()
                    .filter(course -> geocodingService.calculateDistance(
                            latitude, longitude,
                            course.getStartpositionLatitude(), course.getStartpositionLongitude()) <= radiusInKm)
                    .collect(Collectors.toList());
    }

    // REMPLACER la méthode findByPostalCode() dans CourseRepositoryImpl.java

    @Override
    public List<Course> findByPostalCode(String postalCode) {
        System.out.println("=== CourseRepositoryImpl.findByPostalCode ===");
        System.out.println("Code postal recherché: '" + postalCode + "'");

        if (postalCode == null || postalCode.trim().isEmpty()) {
            System.out.println("Code postal vide, retour de toutes les courses");
            return findAll();
        }

        List<Course> allCourses = findAll();
        System.out.println("Total des courses dans la base: " + allCourses.size());

        // *** NOUVELLE LOGIQUE : Utiliser la liste complète des codes postaux ***
        List<Integer> searchCodes = PostalCodeMapper.getAllValidPostalCodes(postalCode);

        if (searchCodes.isEmpty()) {
            System.err.println("❌ Impossible de parser le code postal: '" + postalCode + "'");
            return new ArrayList<>();
        }

        // Rechercher les courses pour TOUS les codes postaux de la liste
        List<Course> matchingCourses = allCourses.stream()
                .filter(course -> searchCodes.contains(course.getZipCode()))
                .collect(Collectors.toList());

        // Affichage des résultats par arrondissement
        if (PostalCodeMapper.shouldExpand(postalCode)) {
            String cityName = PostalCodeMapper.getCityName(postalCode);
            System.out.println("🏙️ Recherche étendue pour " + cityName + " dans " + searchCodes.size() + " arrondissements");

            // Debug : afficher le détail par arrondissement
            Map<Integer, Long> coursesByArrondissement = matchingCourses.stream()
                    .collect(Collectors.groupingBy(Course::getZipCode, Collectors.counting()));

            coursesByArrondissement.forEach((zipCode, count) ->
                    System.out.println("  ✅ " + zipCode + ": " + count + " course(s)"));

            if (coursesByArrondissement.isEmpty()) {
                System.out.println("  ❌ Aucune course trouvée dans les arrondissements de " + cityName);
            }
        } else {
            // Recherche normale
            System.out.println("🔍 Recherche normale pour le code postal: " + searchCodes.get(0));
            if (!matchingCourses.isEmpty()) {
                matchingCourses.forEach(course ->
                        System.out.println("  ✅ Course trouvée: " + course.getName() + " (CP: " + course.getZipCode() + ")"));
            }
        }

        System.out.println("🎯 Total courses trouvées: " + matchingCourses.size());
        System.out.println("=== FIN CourseRepositoryImpl.findByPostalCode ===");

        return matchingCourses;
    }

    @Override
    public List<Course> findByMonth(String month) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE TO_CHAR(startdate, 'MM') = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            // Set the month parameter (e.g., "01" for January)
            stmt.setString(1, month);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    courses.add(Mapper.mapRowToCourse(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    @Override
    public Course save(Course course) {
        String sql = "INSERT INTO PUBLIC.COURSE (name, description, associationid, membercreatorid, startdate, enddate, startpositionlatitude, startpositionlongitude, endpositionlatitude, endpositionlongitude, distance, address, city, zipcode, maxofrunners, currentnumberofrunners, price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, course.getName());
            stmt.setString(2, course.getDescription());
            if (course.getAssociationId() == null || course.getAssociationId() == 0) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, course.getAssociationId());
            }
            stmt.setInt(4, course.getMemberCreatorId());
            stmt.setTimestamp(5, course.getStartDate() != null ?
                    Timestamp.valueOf(course.getStartDate()) : null);
            stmt.setTimestamp(6, course.getEndDate() != null ?
                    Timestamp.valueOf(course.getEndDate()) : null);
            stmt.setDouble(7, course.getStartpositionLatitude());
            stmt.setDouble(8, course.getStartpositionLongitude());
            stmt.setDouble(9, course.getEndpositionLatitude());
            stmt.setDouble(10, course.getEndpositionLongitude());
            stmt.setDouble(11, course.getDistance());
            stmt.setString(12, course.getAddress());
            stmt.setString(13, course.getCity());
            stmt.setInt(14, course.getZipCode());
            stmt.setInt(15, course.getMaxOfRunners());
            stmt.setInt(16, course.getCurrentNumberOfRunners());
            stmt.setDouble(17, course.getPrice());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating course failed, no ID obtained.");
                }
            }

            System.out.println("✅ Course enregistrée : " + course.getName());
            return course;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Course updateCourse(Course course) {
        // Vérifier si l'ID de la course est valide
        if (course == null || course.getId() == null) {
            System.out.println("❌ Course ID est nul, impossible de mettre à jour.");
            return null;
        }
        String sql = "UPDATE course SET name = ?, description = ?, associationid = ?, membercreatorid = ?, startdate = ?, enddate = ?, startpositionlatitude = ?, startpositionlongitude = ?, endpositionlatitude = ?, endpositionlongitude = ?, distance = ?, address = ?, city = ?, zipcode = ?, maxofrunners = ?, currentnumberofrunners = ?, price = ? WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, course.getName());
            stmt.setString(2, course.getDescription());
            if (course.getAssociationId() == null || course.getAssociationId() == 0) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, course.getAssociationId());
            }
            stmt.setInt(4, course.getMemberCreatorId());
            stmt.setTimestamp(5, course.getStartDate()!= null ?
                    Timestamp.valueOf(course.getStartDate()) : null);
            stmt.setTimestamp(6, course.getEndDate() != null ?
                    Timestamp.valueOf(course.getEndDate()) : null);
            stmt.setDouble(7, course.getStartpositionLatitude());
            stmt.setDouble(8, course.getStartpositionLongitude());
            stmt.setDouble(9, course.getEndpositionLatitude());
            stmt.setDouble(10, course.getEndpositionLongitude());
            stmt.setDouble(11, course.getDistance());
            stmt.setString(12, course.getAddress());
            stmt.setString(13, course.getCity());
            stmt.setInt(14, course.getZipCode());
            stmt.setInt(15, course.getMaxOfRunners());
            stmt.setInt(16, course.getCurrentNumberOfRunners());
            stmt.setDouble(17, course.getPrice());
            stmt.setLong(18, course.getId());

            System.out.println("Données de mises à jour" + stmt.toString());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Course mise à jour : " + course.getName());
                return course;
            } else {
                System.out.println("❌ Aucune ligne mise à jour pour la course avec ID : " + course.getId());
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Course> findPastCoursesByAssociationId(Long associationId) {
        // vérfier l'id de l'asso
        if (associationId == null) {
            System.out.println("❌ Association ID est nul, impossible de trouver des courses.");
            return new ArrayList<>();
        }
        List<Course> pastAssoCourses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE associationid = ? AND startdate < CURRENT_TIMESTAMP ORDER BY startdate DESC";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, associationId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                pastAssoCourses.add(Mapper.mapRowToCourse(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (pastAssoCourses.isEmpty()) {
            System.out.println("❌ Aucune course passée trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + pastAssoCourses.size() + " courses passée trouvées dans la base de données.");
        }
        return pastAssoCourses;
    }

    @Override
    public List<Course> findUpcomingCoursesByAssociationId(Long associationId) {
        // vérfier l'id de l'asso
        if (associationId == null) {
            System.out.println("❌ Association ID est nul, impossible de trouver des courses.");
            return new ArrayList<>();
        }
        List<Course> upcomingAssoCourses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE associationid = ? AND startdate > CURRENT_TIMESTAMP ORDER BY startdate ASC";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, associationId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                upcomingAssoCourses.add(Mapper.mapRowToCourse(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (upcomingAssoCourses.isEmpty()) {
            System.out.println("❌ Aucune course à venir trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + upcomingAssoCourses.size() + " courses à venir trouvées dans la base de données.");
        }
        return upcomingAssoCourses;
    }

    @Override
    public void deleteCourse(Long id) {
        String sql = "DELETE FROM course WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Aucune course supprimée. ID inexistant ?");
            }

            System.out.println("✅ Course supprimée avec succès. ID = " + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la course : " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer la course", e);
        }
    }

}