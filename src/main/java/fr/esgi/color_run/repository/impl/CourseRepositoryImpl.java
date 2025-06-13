package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.util.Config;
import fr.esgi.color_run.util.Mapper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CourseRepositoryImpl implements CourseRepository {

    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public CourseRepositoryImpl() {
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
        String sql = "SELECT * FROM course WHERE startdate > CURRENT_TIMESTAMP";
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
        String sql = "SELECT * FROM course WHERE startdate < CURRENT_TIMESTAMP";
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
    public Course findById(Integer id) {
        Course course = null;
        String sql = "SELECT * FROM course WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
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
            sql.append(" AND DATE(startdate) >= ?");
            parameters.add(Date.valueOf(fromDate));
        }

        // Filtre par date de fin
        if (toDate != null) {
            sql.append(" AND DATE(startdate) <= ?");
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
        public Course save(Course course) {
            String sql = "INSERT INTO PUBLIC.COURSE (name, description, associationid, membercreatorid, startdate, enddate, startpositionlatitude, startpositionlongitude, endpositionlatitude, endpositionlongitude, distance, address, city, zipcode, maxofrunners, currentnumberofrunners, price) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, course.getName());
                stmt.setString(2, course.getDescription());
                stmt.setInt(3, course.getAssociationId());
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
                stmt.setInt(3, course.getAssociationId());
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
    }