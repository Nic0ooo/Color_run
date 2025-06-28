package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.Course_memberRepository;
import fr.esgi.color_run.util.Config;
import fr.esgi.color_run.util.DatabaseManager;
import fr.esgi.color_run.util.Mapper;

import java.util.Optional;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Course_memberRepositoryImpl implements Course_memberRepository {

    private final DatabaseManager dbManager;


    public Course_memberRepositoryImpl() {
        this.dbManager = DatabaseManager.getInstance();
        ensureTableExists();
        updateTableForBibNumber();
    }

    private Connection getConnection() throws SQLException {
        return dbManager.getConnection();
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS CourseMember (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "courseId INTEGER," +
                "memberId INTEGER," +
                "registrationDate VARCHAR(255)," +
                "registrationStatus VARCHAR(255)," +
                "stripeSessionId VARCHAR(255) DEFAULT NULL," +
                "FOREIGN KEY (courseId) REFERENCES Course(id)," +
                "FOREIGN KEY (memberId) REFERENCES Member(id)" +
                ");";

        dbManager.ensureTableExists("CourseMember", sql);
    }

    /**
     * Mise à jour de la table pour inclure le champ bibNumber
     */
    private void updateTableForBibNumber() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, "PUBLIC", "COURSEMEMBER", "BIBNUMBER");

            if (!columns.next()) {
                // La colonne n'existe pas, l'ajouter
                String alterSql = "ALTER TABLE CourseMember ADD COLUMN bibNumber VARCHAR(10)";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterSql);
                    System.out.println("✅ Colonne bibNumber ajoutée à CourseMember");
                }
            } else {
                System.out.println("✅ Colonne bibNumber déjà présente dans CourseMember");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de la table pour bibNumber: " + e.getMessage());
        }
    }

    @Override
    public void save(Course_member course_member) {
        // Vérifier si l'inscription existe déjà
        String checkSql = "SELECT id FROM CourseMember WHERE courseId = ? AND memberId = ?";
        String insertSql = "INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId, bibNumber) VALUES (?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE CourseMember SET registrationDate = ?, registrationStatus = ?, stripeSessionId = ?, bibNumber = ? WHERE courseId = ? AND memberId = ?";

        try (Connection conn = getConnection()) {
            // Vérifier si l'inscription existe
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setLong(1, course_member.getCourseId());
                checkStmt.setLong(2, course_member.getMemberId());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Mise à jour
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, course_member.getRegistrationDate());
                        updateStmt.setString(2, course_member.getRegistrationStatus() != null ?
                                course_member.getRegistrationStatus().name() : "PENDING");
                        updateStmt.setString(3, course_member.getStripeSessionId());
                        updateStmt.setString(4, course_member.getBibNumber());
                        updateStmt.setLong(5, course_member.getCourseId());
                        updateStmt.setLong(6, course_member.getMemberId());

                        updateStmt.executeUpdate();
                        System.out.println("✅ Course_member mis à jour - Course: " + course_member.getCourseId() +
                                ", Member: " + course_member.getMemberId() + ", Dossard: " + course_member.getBibNumber());
                    }
                } else {
                    // Insertion
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setLong(1, course_member.getCourseId());
                        insertStmt.setLong(2, course_member.getMemberId());
                        insertStmt.setString(3, course_member.getRegistrationDate() != null ?
                                course_member.getRegistrationDate() : java.time.LocalDateTime.now().toString());
                        insertStmt.setString(4, course_member.getRegistrationStatus() != null ?
                                course_member.getRegistrationStatus().name() : "PENDING");
                        insertStmt.setString(5, course_member.getStripeSessionId());
                        insertStmt.setString(6, course_member.getBibNumber());

                        insertStmt.executeUpdate();

                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                course_member.setId(generatedKeys.getLong(1));
                            }
                        }

                        System.out.println("✅ Course_member créé - Course: " + course_member.getCourseId() +
                                ", Member: " + course_member.getMemberId() + ", Dossard: " + course_member.getBibNumber());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la sauvegarde du Course_member: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtient les détails de l'inscription d'un membre pour une course
     */
    public Optional<Course_member> getRegistrationDetails(long courseId, long memberId) {
        String sql = "SELECT * FROM CourseMember WHERE courseId = ? AND memberId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, courseId);
            pstmt.setLong(2, memberId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Course_member courseMember = Mapper.mapRowToCourse_member(rs);
                System.out.println("✅ Détails inscription trouvés - Course: " + courseId +
                        ", Member: " + memberId + ", Status: " + courseMember.getRegistrationStatus() +
                        ", Dossard: " + courseMember.getBibNumber());
                return Optional.of(courseMember);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des détails d'inscription: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Sauvegarde avec support Stripe (INSERT ou UPDATE automatique)
     */
    public void saveWithStripe(Course_member course_member) {
        if (course_member.getId() != null && course_member.getId() > 0) {
            updateWithStripe(course_member);
        } else {
            insertWithStripe(course_member);
        }
    }

    /**
     * INSERT pour Stripe avec bibNumber
     */
    private void insertWithStripe(Course_member course_member) {
        String sql = "INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId, bibNumber) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, course_member.getCourseId());
            pstmt.setLong(2, course_member.getMemberId());
            pstmt.setString(3, course_member.getRegistrationDate());
            pstmt.setString(4, course_member.getRegistrationStatus() != null ?
                    course_member.getRegistrationStatus().name() : "PENDING");
            pstmt.setString(5, course_member.getStripeSessionId());
            pstmt.setString(6, course_member.getBibNumber());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course_member.setId(generatedKeys.getLong(1));
                }
            }

            System.out.println("✅ Inscription Stripe insérée - ID: " + course_member.getId() +
                    ", Session: " + course_member.getStripeSessionId() + ", Dossard: " + course_member.getBibNumber());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion Stripe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * UPDATE pour Stripe avec bibNumber
     */
    private void updateWithStripe(Course_member course_member) {
        String sql = "UPDATE CourseMember SET registrationStatus = ?, registrationDate = ?, bibNumber = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course_member.getRegistrationStatus() != null ?
                    course_member.getRegistrationStatus().name() : "PENDING");
            pstmt.setString(2, course_member.getRegistrationDate());
            pstmt.setString(3, course_member.getBibNumber());
            pstmt.setLong(4, course_member.getId());

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✅ Inscription Stripe mise à jour - ID: " + course_member.getId() +
                        ", Status: " + course_member.getRegistrationStatus() + ", Dossard: " + course_member.getBibNumber());
            } else {
                System.err.println("❌ Aucune ligne mise à jour pour l'inscription: " + course_member.getId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour Stripe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Trouve une inscription par session Stripe
     */
    public Optional<Course_member> findByStripeSessionId(String stripeSessionId) {
        String sql = "SELECT * FROM CourseMember WHERE stripeSessionId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, stripeSessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Course_member courseMember = Mapper.mapRowToCourse_member(rs);
                System.out.println("✅ Inscription trouvée par Stripe session: " + stripeSessionId);
                return Optional.of(courseMember);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par stripeSessionId: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Vérification d'inscription avec statut ACCEPTED (payé)
     */
    public boolean isMemberRegisteredAndPaid(long courseId, long memberId) {
        String sql = "SELECT COUNT(*) FROM CourseMember WHERE courseId = ? AND memberId = ? AND registrationStatus = 'ACCEPTED'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, courseId);
            pstmt.setLong(2, memberId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean isPaid = rs.getInt(1) > 0;
                System.out.println("🔍 Vérification paiement - Course: " + courseId +
                        ", Member: " + memberId + ", Payé: " + isPaid);
                return isPaid;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de paiement: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    @Override
    public void delete(Course_member course_member) {
        String sql = "DELETE FROM CourseMember WHERE courseId = ? AND memberId = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, course_member.getCourseId());
            pstmt.setLong(2, course_member.getMemberId());
            int deleted = pstmt.executeUpdate();

            if (deleted > 0) {
                System.out.println("✅ Course_member supprimé - Course: " + course_member.getCourseId() +
                        ", Member: " + course_member.getMemberId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du membre de la course :");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isMemberInCourse(long courseId, long memberId) {
        String sql = "SELECT COUNT(*) FROM CourseMember WHERE courseId = ? AND memberId = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, courseId);
            pstmt.setLong(2, memberId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isInCourse = rs.getInt(1) > 0;
                System.out.println("🔍 Vérification inscription - Course: " + courseId +
                        ", Member: " + memberId + ", Inscrit: " + isInCourse);
                return isInCourse;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de l'appartenance du membre à la course :");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Course> findCoursesByMemberId(long memberId) {
        String sql = "SELECT c.* FROM course c JOIN CourseMember cm ON c.id = cm.courseId WHERE cm.memberId = ? AND cm.registrationStatus = 'ACCEPTED'";
        List<Course> courses = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Mapper.mapRowToCourse(rs));
            }

            System.out.println("✅ " + courses.size() + " courses trouvées pour member " + memberId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des courses pour le membre :");
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public List<Course> findPastCoursesByMemberId(long memberId) {
        String sql = "SELECT c.* FROM course c JOIN CourseMember cm ON c.id = cm.courseId " +
                "WHERE cm.memberId = ? AND cm.registrationStatus = 'ACCEPTED' AND c.enddate < NOW()";
        List<Course> courses = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Mapper.mapRowToCourse(rs));
            }

            System.out.println("✅ " + courses.size() + " courses passées trouvées pour member " + memberId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des courses passées pour le membre :");
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public List<Course> findUpcomingCoursesByMemberId(long memberId) {
//        String sql = "SELECT c.* FROM course c JOIN CourseMember cm ON c.id = cm.courseId WHERE cm.memberId = ? AND c.enddate > NOW()";
        String sql = "SELECT c.* FROM course c JOIN CourseMember cm ON c.id = cm.courseId " +
                "WHERE cm.memberId = ? AND cm.registrationStatus = 'ACCEPTED' AND c.startdate > NOW()";
        List<Course> courses = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Mapper.mapRowToCourse(rs));
            }

            System.out.println("✅ " + courses.size() + " courses à venir trouvées pour member " + memberId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des courses à venir pour le membre :");
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public List<Member> findMembersByCourseId(long courseId) {
        String sql = "SELECT m.* FROM member m JOIN CourseMember cm ON m.id = cm.memberId " +
                "WHERE cm.courseId = ? AND cm.registrationStatus = 'ACCEPTED'";
        List<Member> members = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(Mapper.mapRowToMember(rs));
            }

            System.out.println("✅ " + members.size() + " membres trouvés pour course " + courseId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des membres de la course :");
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Méthodes utilitaires pour les statistiques
     */
    public int countRegistrationsByCourse(long courseId) {
        String sql = "SELECT COUNT(*) FROM CourseMember WHERE courseId = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des inscriptions: " + e.getMessage());
        }
        return 0;
    }

    public int countPaidRegistrationsByCourse(long courseId) {
        String sql = "SELECT COUNT(*) FROM CourseMember WHERE courseId = ? AND registrationStatus = 'ACCEPTED'";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des inscriptions payées: " + e.getMessage());
        }
        return 0;
    }

    public int countBibNumbersByCourse(long courseId) {
        String sql = "SELECT COUNT(*) FROM CourseMember WHERE courseId = ? AND bibNumber IS NOT NULL AND bibNumber != ''";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des dossards: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Affiche les statistiques d'une course
     */
    public void printCourseStats(long courseId) {
        int totalRegistrations = countRegistrationsByCourse(courseId);
        int paidRegistrations = countPaidRegistrationsByCourse(courseId);
        int bibNumbers = countBibNumbersByCourse(courseId);

        System.out.println("📊 Statistiques Course " + courseId + ":");
        System.out.println("   - Inscriptions totales: " + totalRegistrations);
        System.out.println("   - Inscriptions payées: " + paidRegistrations);
        System.out.println("   - Dossards générés: " + bibNumbers);
        System.out.println("   - Taux de paiement: " + (totalRegistrations > 0 ?
                Math.round((double) paidRegistrations / totalRegistrations * 100) : 0) + "%");
        System.out.println("   - Taux de génération dossards: " + (paidRegistrations > 0 ?
                Math.round((double) bibNumbers / paidRegistrations * 100) : 0) + "%");
    }

    // Dans Course_memberRepositoryImpl.java - Ajouter cette méthode

    /**
     * Compte le nombre de membres inscrits et payés pour une course
     */
    public int countRegisteredAndPaidMembers(Long courseId) {
        String sql = "SELECT COUNT(*) FROM CourseMember WHERE courseId = ? AND registrationStatus = 'ACCEPTED'";

        try (Connection connection = getConnection(); // ✅ Utiliser getConnection() au lieu de DatabaseConnection
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    System.out.println("🔢 Nombre d'inscrits payés pour la course " + courseId + ": " + count);
                    return count;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des inscrits: " + e.getMessage());
            e.printStackTrace();
        }


        return 0;
    }
}
