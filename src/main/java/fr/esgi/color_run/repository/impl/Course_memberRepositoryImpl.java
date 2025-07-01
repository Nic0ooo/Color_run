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
     * Sauvegarde avec support Stripe - TOUJOURS INSERT pour nouvelles sessions Stripe
     */
    public void saveWithStripe(Course_member course_member) {
        System.out.println("🔧 saveWithStripe appelé - Course: " + course_member.getCourseId() +
                ", Member: " + course_member.getMemberId() +
                ", Session: " + course_member.getStripeSessionId() +
                ", ID actuel: " + course_member.getId());

        // CORRECTION: Vérifier d'abord si une inscription existe déjà
        if (course_member.getStripeSessionId() != null && !course_member.getStripeSessionId().trim().isEmpty()) {

            // 1. Chercher une inscription existante par session Stripe
            Optional<Course_member> existingBySession = findByStripeSessionId(course_member.getStripeSessionId());
            if (existingBySession.isPresent()) {
                System.out.println("✅ Inscription trouvée par session Stripe, mise à jour...");
                Course_member existing = existingBySession.get();

                // Mettre à jour uniquement les champs nécessaires
                existing.setRegistrationStatus(course_member.getRegistrationStatus());
                if (course_member.getBibNumber() != null) {
                    existing.setBibNumber(course_member.getBibNumber());
                }

                updateWithStripe(existing);

                // Copier l'ID vers l'objet original pour cohérence
                course_member.setId(existing.getId());
                return;
            }

            // 2. Chercher une inscription existante par course/member (cas PENDING → ACCEPTED)
            Optional<Course_member> existingByCourseMember = getRegistrationDetails(
                    course_member.getCourseId(),
                    course_member.getMemberId()
            );

            if (existingByCourseMember.isPresent()) {
                System.out.println("✅ Inscription course/member existante, mise à jour avec session Stripe...");
                Course_member existing = existingByCourseMember.get();

                // Mettre à jour avec les nouvelles informations Stripe
                existing.setRegistrationStatus(course_member.getRegistrationStatus());
                existing.setStripeSessionId(course_member.getStripeSessionId());
                if (course_member.getBibNumber() != null) {
                    existing.setBibNumber(course_member.getBibNumber());
                }

                updateWithStripe(existing);

                // Copier l'ID vers l'objet original
                course_member.setId(existing.getId());
                return;
            }

            // 3. Aucune inscription existante → Créer une nouvelle
            System.out.println("🆕 Aucune inscription existante, création...");
            course_member.setId(null); // Reset ID pour forcer INSERT
            insertWithStripe(course_member);

        } else {
            // Cas normal (pas Stripe) : logique habituelle
            if (course_member.getId() != null && course_member.getId() > 0) {
                updateWithStripe(course_member);
            } else {
                insertWithStripe(course_member);
            }
        }
    }

    /**
     * INSERT pour Stripe avec bibNumber
     */
    /**
     * INSERT pour Stripe avec commit forcé
     */
    private void insertWithStripe(Course_member course_member) {
        String sql = "INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId, bibNumber) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();

            // Désactiver l'auto-commit pour contrôler la transaction
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setLong(1, course_member.getCourseId());
            pstmt.setLong(2, course_member.getMemberId());
            pstmt.setString(3, course_member.getRegistrationDate());
            pstmt.setString(4, course_member.getRegistrationStatus() != null ?
                    course_member.getRegistrationStatus().name() : "PENDING");
            pstmt.setString(5, course_member.getStripeSessionId());
            pstmt.setString(6, course_member.getBibNumber());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        course_member.setId(generatedKeys.getLong(1));
                    }
                }

                // FORCER LE COMMIT IMMÉDIATEMENT
                conn.commit();

                System.out.println("✅ Inscription Stripe insérée et commitée - ID: " + course_member.getId() +
                        ", Session: " + course_member.getStripeSessionId() + ", Dossard: " + course_member.getBibNumber());
            } else {
                conn.rollback();
                System.err.println("❌ Aucune ligne insérée pour l'inscription Stripe");
            }

            // Remettre l'auto-commit à sa valeur originale
            conn.setAutoCommit(originalAutoCommit);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion Stripe: " + e.getMessage());
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(true); // Remettre l'auto-commit par défaut
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Erreur lors du rollback: " + rollbackEx.getMessage());
                }
            }
        } finally {
            // Fermer les ressources
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.err.println("❌ Erreur fermeture PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("❌ Erreur fermeture Connection: " + e.getMessage());
                }
            }
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
    /**
     * Trouve une inscription par session Stripe avec debug
     */
    public Optional<Course_member> findByStripeSessionId(String stripeSessionId) {
        String sql = "SELECT * FROM CourseMember WHERE stripeSessionId = ?";

        // Debug: Afficher toutes les sessions en base
        debugStripeSessionIds(stripeSessionId);

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, stripeSessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Course_member courseMember = Mapper.mapRowToCourse_member(rs);
                System.out.println("✅ Inscription trouvée par Stripe session: " + stripeSessionId);
                return Optional.of(courseMember);
            } else {
                System.out.println("❌ Aucune inscription trouvée pour session: " + stripeSessionId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par stripeSessionId: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Debug pour voir toutes les sessions Stripe en base
     */
    private void debugStripeSessionIds(String searchedSessionId) {
        String sql = "SELECT id, courseId, memberId, stripeSessionId, registrationStatus FROM CourseMember WHERE stripeSessionId IS NOT NULL ORDER BY id DESC LIMIT 10";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            System.out.println("🔍 DEBUG: Dernières sessions Stripe en base:");
            boolean found = false;
            int count = 0;

            while (rs.next()) {
                count++;
                String sessionId = rs.getString("stripeSessionId");
                System.out.println("  " + count + ". ID: " + rs.getLong("id") +
                        ", Course: " + rs.getLong("courseId") +
                        ", Member: " + rs.getLong("memberId") +
                        ", Session: " + sessionId +
                        ", Status: " + rs.getString("registrationStatus"));

                if (searchedSessionId.equals(sessionId)) {
                    found = true;
                    System.out.println("      ✅ SESSION RECHERCHÉE TROUVÉE!");
                }
            }

            if (count == 0) {
                System.out.println("  ❌ Aucune session Stripe trouvée en base");
            } else if (!found) {
                System.out.println("  ❌ Session recherchée '" + searchedSessionId + "' NON TROUVÉE parmi les " + count + " dernières");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du debug des sessions: " + e.getMessage());
        }
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
