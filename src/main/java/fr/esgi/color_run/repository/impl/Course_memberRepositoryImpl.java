package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.Course_memberRepository;
import fr.esgi.color_run.util.Config;
import fr.esgi.color_run.util.Mapper;

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
            String sql = "INSERT INTO course_member (course_id, member_id) VALUES (?, ?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, course_member.getCourseId());
                pstmt.setLong(2, course_member.getMemberId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de l'insertion du membre dans la course :");
                e.printStackTrace();
            }
        }

        @Override
        public void delete(Course_member course_member) {
            String sql = "DELETE FROM course_member WHERE course_id = ? AND member_id = ?";
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
            String sql = "SELECT COUNT(*) FROM course_member WHERE course_id = ? AND member_id = ?";
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
            String sql = "SELECT c.* FROM course c JOIN course_member cm ON c.id = cm.course_id WHERE cm.member_id = ?";
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
            String sql = "SELECT c.* FROM course c JOIN course_member cm ON c.id = cm.course_id WHERE cm.member_id = ? AND c.end_date < NOW()";
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
        String sql = "SELECT c.* FROM course c JOIN course_member cm ON c.id = cm.course_id WHERE cm.member_id = ? AND c.end_date > NOW()";
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
            String sql = "SELECT m.* FROM member m JOIN course_member cm ON m.id = cm.member_id WHERE cm.course_id = ?";
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
