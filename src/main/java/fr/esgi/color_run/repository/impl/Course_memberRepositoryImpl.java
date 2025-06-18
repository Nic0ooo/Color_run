package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.Course_memberRepository;
import fr.esgi.color_run.util.Config;
import fr.esgi.color_run.util.Mapper;

import java.util.Optional;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Course_memberRepositoryImpl implements Course_memberRepository {
    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public Course_memberRepositoryImpl() {
            try {
                // Obligatoire pour que Tomcat charge le driver H2
                Class.forName("org.h2.Driver");
                System.out.println("✅ Driver H2 chargé");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver H2 introuvable !");
                e.printStackTrace();
            }

            testDatabaseConnection();
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

        @Override
        public void save(Course_member course_member) {
            String sql = "INSERT INTO CourseMember (courseId, memberId) VALUES (?, ?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, course_member.getCourseId());
                pstmt.setLong(2, course_member.getMemberId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de l'insertion du membre dans la course :");
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
                return Optional.of(Mapper.mapRowToCourse_member(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des détails d'inscription: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

        /**
         * 🆕 Méthode save pour Stripe (INSERT ou UPDATE automatique)
         */
        public void saveWithStripe(Course_member course_member) {
            // Si l'ID existe, faire un UPDATE, sinon INSERT
            if (course_member.getId() != null && course_member.getId() > 0) {
                updateWithStripe(course_member);
            } else {
                insertWithStripe(course_member);
            }
        }

        /**
         * INSERT pour Stripe
         */
        private void insertWithStripe(Course_member course_member) {
            String sql = "INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setLong(1, course_member.getCourseId());
                pstmt.setLong(2, course_member.getMemberId());
                pstmt.setString(3, course_member.getRegistrationDate());
                pstmt.setString(4, course_member.getRegistrationStatus().name());
                pstmt.setString(5, course_member.getStripeSessionId());

                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        course_member.setId(generatedKeys.getLong(1));
                    }
                }

                System.out.println("✅ Inscription Stripe insérée: " + course_member.getId());

            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de l'insertion Stripe: " + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * UPDATE pour Stripe
         */
        private void updateWithStripe(Course_member course_member) {
            String sql = "UPDATE CourseMember SET registrationStatus = ?, registrationDate = ? WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, course_member.getRegistrationStatus().name());
                pstmt.setString(2, course_member.getRegistrationDate());
                pstmt.setLong(3, course_member.getId());

                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("✅ Inscription Stripe mise à jour: " + course_member.getId());
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
                    return Optional.of(Mapper.mapRowToCourse_member(rs));
                }

            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la recherche par stripeSessionId: " + e.getMessage());
                e.printStackTrace();
            }

            return Optional.empty();
        }

        /**
         * Vérification d'inscription avec statut ACCEPTED
         */
        public boolean isMemberRegisteredAndPaid(long courseId, long memberId) {
            String sql = "SELECT COUNT(*) FROM CourseMember WHERE courseId = ? AND memberId = ? AND registrationStatus = 'ACCEPTED'";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setLong(1, courseId);
                pstmt.setLong(2, memberId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0;
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
                pstmt.executeUpdate();
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
                    return rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la vérification de l'appartenance du membre à la course :");
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public List<Course> findCoursesByMemberId(long memberId) {
            String sql = "SELECT c.* FROM course c JOIN CourseMember cm ON c.id = cm.courseId WHERE cm.memberId = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, memberId);
                ResultSet rs = pstmt.executeQuery();
                List<Course> courses = new ArrayList<>();
                while (rs.next()) {
                    Course course = new Course();
                    course.setId(rs.getLong("id"));
                    course.setName(rs.getString("name"));
                    // Map other fields as necessary
                    courses.add(course);
                }
                return courses;
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la récupération des courses pour le membre :");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public List<Course> findPastCoursesByMemberId(long memberId) {
            String sql = "SELECT c.* FROM course c JOIN CourseMember cm ON c.id = cm.courseId WHERE cm.memberId = ? AND c.end_date < NOW()";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, memberId);
                ResultSet rs = pstmt.executeQuery();
                List<Course> courses = new ArrayList<>();
                while (rs.next()) {
                    courses.add(Mapper.mapRowToCourse(rs));
                }
                return courses;
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la récupération des courses passées pour le membre :");
                e.printStackTrace();
            }
            return null;
        }

    @Override
    public List<Course> findUpcomingCoursesByMemberId(long memberId) {
        String sql = "SELECT c.* FROM course c JOIN CourseMember cm ON c.id = cm.courseId WHERE cm.memberId = ? AND c.end_date > NOW()";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            List<Course> courses = new ArrayList<>();
            while (rs.next()) {
                courses.add(Mapper.mapRowToCourse(rs));
            }
            return courses;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des courses à venir pour le membre :");
            e.printStackTrace();
        }
        return null;
    }

        @Override
        public List<Member> findMembersByCourseId(long courseId) {
            String sql = "SELECT m.* FROM member m JOIN CourseMember cm ON m.id = cm.memberId WHERE cm.courseId = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, courseId);
                ResultSet rs = pstmt.executeQuery();
                List<Member> members = new ArrayList<>();
                while (rs.next()) {
                    members.add(Mapper.mapRowToMember(rs));
                }
                return members;
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la récupération des membres de la course :");
                e.printStackTrace();
            }
            return null;
        }


}
