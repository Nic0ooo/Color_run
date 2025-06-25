package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Discussion;
import fr.esgi.color_run.repository.DiscussionRepository;
import fr.esgi.color_run.util.Config;

import java.sql.*;
        import java.util.Optional;

public class DiscussionRepositoryImpl implements DiscussionRepository {

    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public DiscussionRepositoryImpl() {
        try {
            Class.forName("org.h2.Driver");
            System.out.println("✅ Driver H2 chargé pour DiscussionRepository");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver H2 introuvable !");
            e.printStackTrace();
        }

        createTableIfNotExists();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS Discussion (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "courseId INTEGER," +
                "isActive BOOLEAN DEFAULT TRUE," +
                "FOREIGN KEY (courseId) REFERENCES Course(id)" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'Discussion' vérifiée/créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table Discussion:");
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Discussion> findByCourseId(Long courseId) {
        String sql = "SELECT * FROM Discussion WHERE courseId = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDiscussion(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche de discussion:");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Discussion createForCourse(Long courseId) {
        String sql = "INSERT INTO Discussion (courseId, isActive) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, courseId);
            ps.setBoolean(2, true);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Discussion discussion = new Discussion();
                    discussion.setId(rs.getLong(1));
                    discussion.setCourseId(courseId);
                    discussion.setActive(true);

                    System.out.println("✅ Discussion créée pour la course : " + courseId);
                    return discussion;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de discussion:");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Discussion updateActiveStatus(Long discussionId, boolean isActive) {
        String sql = "UPDATE Discussion SET isActive = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isActive);
            ps.setLong(2, discussionId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                // Récupérer la discussion mise à jour
                return findById(discussionId).orElse(null);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de discussion:");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Optional<Discussion> findById(Long id) {
        String sql = "SELECT * FROM Discussion WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDiscussion(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche de discussion par ID:");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private Discussion mapRowToDiscussion(ResultSet rs) throws SQLException {
        Discussion discussion = new Discussion();
        discussion.setId(rs.getLong("id"));
        discussion.setCourseId(rs.getLong("courseId"));
        discussion.setActive(rs.getBoolean("isActive"));
        return discussion;
    }
}