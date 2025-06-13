// File: src/main/java/fr/esgi/color_run/repository/impl/CourseRepositoryImpl.java
package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.util.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.sql.DriverManager.getConnection;

public class CourseRepositoryImpl implements CourseRepository {

/*
    private final String jdbcUrl = "jdbc:h2:./db_file/color_run";
*/
    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";
    private final GeocodingService geocodingService;

    public CourseRepositoryImpl(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;

        try {
            // Obligatoire pour que Tomcat charge le driver H2
            Class.forName("org.h2.Driver");
            System.out.println("✅ Driver H2 chargé");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver H2 introuvable !");
            e.printStackTrace();
        }

        testDatabaseConnection();
        createTableIfNotExists();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    public void testDatabaseConnection() {
        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Connexion à la base de données réussie !");
            } else {
                System.out.println("❌ Échec de la connexion à la base de données.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la tentative de connexion à la base de données :");
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS course (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(255)," +
                "description VARCHAR(255)," +
                "associationid INT," +
                "membercreatorid INT," +
                "startdate VARCHAR(255)," +
                "enddate VARCHAR(255)," +
                "startpositionlatitude DOUBLE," +
                "startpositionlongitude DOUBLE," +
                "endpositionlatitude DOUBLE," +
                "endpositionlongitude DOUBLE," +
                "address VARCHAR(255)," +
                "city VARCHAR(255)," +
                "zipcode INT," +
                "maxofrunners INT," +
                "currentnumberofrunners INT," +
                "price DOUBLE" +
                ");";

        try(Connection con = getConnection(); Statement stmt = con.createStatement()) {
            // Vérifier si la table existe déjà
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet tables = metaData.getTables(null, "PUBLIC", "COURSE", null);
            boolean tableExists = tables.next();

            // Exécuter la création de table si nécessaire
            stmt.execute(sql);

            if (tableExists) {
                System.out.println("✅ Table 'course' existe déjà");
            } else {
                System.out.println("✅ Table 'course' créée avec succès");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table :");
            e.printStackTrace();
        }
    }

    @Override
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        System.out.println("CourseRepositoryImpl: findAll() - Exécution de la requête pour récupérer toutes les courses.");
        String sql = "SELECT * FROM course";

        try (Connection connection = getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                courses.add(mapRowToCourse(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (courses.isEmpty()) {
            System.out.println("❌ Aucune course trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + courses.size() + " courses trouvées dans la base de données.");
            System.out.println("CourseRepositoryImpl: findAll() Courses trouvées en base: " + courses);
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

    @Override
    public List<Course> findByPostalCode(String postalCode) {
        // Vérifier si le code postal est vide ou null
        if (postalCode == null || postalCode.trim().isEmpty()) {
            return findAll();
        }

        List<Course> allCourses = findAll();
        return allCourses.stream()
                .filter(course -> course.getZipCode() == Integer.parseInt(postalCode))
                .collect(Collectors.toList());
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
                    courses.add(mapRowToCourse(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    @Override
    public Course save(Course course) {
        String sql = "INSERT INTO PUBLIC.COURSE (name, description, associationid, membercreatorid, startdate, enddate, startpositionlatitude, startpositionlongitude, endpositionlatitude, endpositionlongitude, address, city, zipcode, maxofrunners, currentnumberofrunners, price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();

             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, course.getName());
            stmt.setString(2, course.getDescription());
            stmt.setInt(3, course.getAssociationId());
            stmt.setInt(4, course.getMemberCreatorId());
            stmt.setString(5, course.getStartDate());
            stmt.setString(6, course.getEndDate());
            stmt.setDouble(7, course.getStartpositionLatitude());
            stmt.setDouble(8, course.getStartpositionLongitude());
            stmt.setDouble(9, course.getEndpositionLatitude());
            stmt.setDouble(10, course.getEndpositionLongitude());
            stmt.setString(11, course.getAddress());
            stmt.setString(12, course.getCity());
            stmt.setInt(13, course.getZipCode());
            stmt.setInt(14, course.getMaxOfRunners());
            stmt.setInt(15, course.getCurrentNumberOfRunners());
            stmt.setDouble(16, course.getPrice());

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
        String sql = "UPDATE course SET name = ?, description = ?, associationid = ?, membercreatorid = ?, startdate = ?, enddate = ?, startpositionlatitude = ?, startpositionlongitude = ?, endpositionlatitude = ?, endpositionlongitude = ?, address = ?, city = ?, zipcode = ?, maxofrunners = ?, currentnumberofrunners = ?, price = ? WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, course.getName());
            stmt.setString(2, course.getDescription());
            stmt.setInt(3, course.getAssociationId());
            stmt.setInt(4, course.getMemberCreatorId());
            stmt.setString(5, course.getStartDate());
            stmt.setString(6, course.getEndDate());
            stmt.setDouble(7, course.getStartpositionLatitude());
            stmt.setDouble(8, course.getStartpositionLongitude());
            stmt.setDouble(9, course.getEndpositionLatitude());
            stmt.setDouble(10, course.getEndpositionLongitude());
            stmt.setString(11, course.getAddress());
            stmt.setString(12, course.getCity());
            stmt.setInt(13, course.getZipCode());
            stmt.setInt(14, course.getMaxOfRunners());
            stmt.setInt(15, course.getCurrentNumberOfRunners());
            stmt.setDouble(16, course.getPrice());
            stmt.setLong(17, course.getId());

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

    private Course mapRowToCourse(ResultSet resultSet) throws SQLException {
        Course course = new Course();
        course.setId(resultSet.getLong("id"));
        course.setName(resultSet.getString("name"));
        course.setDescription(resultSet.getString("description"));
        course.setAssociationId(resultSet.getInt("associationid"));
        course.setMemberCreatorId(resultSet.getInt("membercreatorid"));
        course.setStartDate(resultSet.getString("startdate"));
        course.setEndDate(resultSet.getString("enddate"));
        course.setStartpositionLatitude(resultSet.getDouble("startpositionlatitude"));
        course.setStartpositionLongitude(resultSet.getDouble("startpositionlongitude"));
        course.setEndpositionLatitude(resultSet.getDouble("endpositionlatitude"));
        course.setEndpositionLongitude(resultSet.getDouble("endpositionlongitude"));
        course.setAddress(resultSet.getString("address"));
        course.setCity(resultSet.getString("city"));
        course.setZipCode(resultSet.getInt("zipcode"));
        course.setMaxOfRunners(resultSet.getInt("maxofrunners"));
        course.setCurrentNumberOfRunners(resultSet.getInt("currentnumberofrunners"));
        course.setPrice(resultSet.getDouble("price"));
        return course;
    }

    @Override
    public Course findById(Long id) {
        String sql = "SELECT * FROM course WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToCourse(resultSet);
                } else {
                    System.out.println("❌ Aucune course trouvée avec l'ID : " + id);
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}