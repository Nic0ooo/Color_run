// MessageRepositoryImpl.java
package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Message;
import fr.esgi.color_run.repository.MessageRepository;
import fr.esgi.color_run.util.Config;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageRepositoryImpl implements MessageRepository {

    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public MessageRepositoryImpl() {
        try {
            Class.forName("org.h2.Driver");
            System.out.println("Driver H2 chargé pour MessageRepository");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver H2 introuvable pour MessageRepository !");
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
                System.out.println("Connexion à la base de données réussie pour MessageRepository !");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la tentative de connexion à la base de données pour MessageRepository :");
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS Message (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "discussionId INTEGER," +
                "memberId INTEGER," +
                "content VARCHAR(1000)," +
                "originalContent VARCHAR(1000)," +
                "date TIMESTAMP," +
                "lastModifiedDate TIMESTAMP," +
                "isPin BOOLEAN DEFAULT FALSE," +
                "isHidden BOOLEAN DEFAULT FALSE," +
                "isModified BOOLEAN DEFAULT FALSE," +
                "isDeleted BOOLEAN DEFAULT FALSE," +
                "hiddenByMemberId INTEGER," +
                "FOREIGN KEY (discussionId) REFERENCES Discussion(id)," +
                "FOREIGN KEY (memberId) REFERENCES Member(id)" +
                ");";

        try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet tables = metaData.getTables(null, "PUBLIC", "MESSAGE", null);
            boolean tableExists = tables.next();

            if (!tableExists) {
                stmt.execute(sql);
                System.out.println("Table 'Message' créée avec succès");
            } else {
                // Vérifier et ajouter les nouvelles colonnes si elles n'existent pas
                addColumnIfNotExists(con, "originalContent", "VARCHAR(1000)");
                addColumnIfNotExists(con, "lastModifiedDate", "TIMESTAMP");
                addColumnIfNotExists(con, "isModified", "BOOLEAN DEFAULT FALSE");
                addColumnIfNotExists(con, "isDeleted", "BOOLEAN DEFAULT FALSE");
                addColumnIfNotExists(con, "hiddenByMemberId", "INTEGER");
                System.out.println("Table 'Message' existe déjà - colonnes vérifiées");
            }
        } catch (SQLException e) {
            System.err.println("Erreur création/mise à jour table Message :");
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour ajouter une colonne si elle n'existe pas
     */
    private void addColumnIfNotExists(Connection con, String columnName, String columnDefinition) {
        try {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet columns = metaData.getColumns(null, "PUBLIC", "MESSAGE", columnName.toUpperCase());

            if (!columns.next()) {
                String alterSql = "ALTER TABLE Message ADD COLUMN " + columnName + " " + columnDefinition;
                try (Statement stmt = con.createStatement()) {
                    stmt.execute(alterSql);
                    System.out.println("Colonne '" + columnName + "' ajoutée à la table Message");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la colonne " + columnName + ": " + e.getMessage());
        }
    }

    @Override
    public Message save(Message message) {
        String sql = "INSERT INTO Message (discussionId, memberId, content, originalContent, date, " +
                "lastModifiedDate, isPin, isHidden, isModified, isDeleted, hiddenByMemberId) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, message.getDiscussionId());
            stmt.setLong(2, message.getMemberId());
            stmt.setString(3, message.getContent());
            stmt.setString(4, message.getOriginalContent());
            stmt.setTimestamp(5, Timestamp.valueOf(message.getDate()));

            if (message.getLastModifiedDate() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(message.getLastModifiedDate()));
            } else {
                stmt.setTimestamp(6, null);
            }

            stmt.setBoolean(7, message.isPin());
            stmt.setBoolean(8, message.isHidden());
            stmt.setBoolean(9, message.isModified());
            stmt.setBoolean(10, message.isDeleted());

            if (message.getHiddenByMemberId() != null) {
                stmt.setLong(11, message.getHiddenByMemberId());
            } else {
                stmt.setNull(11, java.sql.Types.BIGINT);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    message.setId(generatedKeys.getLong(1));
                }
            }

            System.out.println("Message sauvegardé : ID=" + message.getId());
            return message;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du message :");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Message> findByDiscussionId(Long discussionId) {
        String sql = "SELECT m.*, mem.firstname, mem.name " +
                "FROM Message m " +
                "INNER JOIN Member mem ON m.memberId = mem.id " +
                "WHERE m.discussionId = ? AND m.isDeleted = FALSE " +
                "ORDER BY m.isPin DESC, m.date ASC";

        return executeQueryWithJoin(sql, discussionId);
    }

    @Override
    public List<Message> findByDiscussionIdSinceId(Long discussionId, Long sinceId) {
        String sql = "SELECT m.*, mem.firstname, mem.name " +
                "FROM Message m " +
                "INNER JOIN Member mem ON m.memberId = mem.id " +
                "WHERE m.discussionId = ? AND m.id > ? AND m.isDeleted = FALSE " +
                "ORDER BY m.isPin DESC, m.date ASC";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, discussionId);
            stmt.setLong(2, sinceId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(mapRowToMessageWithMember(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des nouveaux messages :");
            e.printStackTrace();
        }

        return messages;
    }

    @Override
    public List<Message> findRecentByDiscussionId(Long discussionId, int limit) {
        String sql = "SELECT m.*, mem.firstname, mem.name " +
                "FROM Message m " +
                "INNER JOIN Member mem ON m.memberId = mem.id " +
                "WHERE m.discussionId = ? AND m.isDeleted = FALSE " +
                "ORDER BY m.isPin DESC, m.date DESC LIMIT ?";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, discussionId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(mapRowToMessageWithMember(rs));
            }

            // Inverser l'ordre pour avoir les plus anciens en premier (sauf pins)
            messages.sort((m1, m2) -> {
                if (m1.isPin() && !m2.isPin()) return -1;
                if (!m1.isPin() && m2.isPin()) return 1;
                return m1.getDate().compareTo(m2.getDate());
            });

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages récents :");
            e.printStackTrace();
        }

        return messages;
    }

    @Override
    public Optional<Message> findById(Long id) {
        String sql = "SELECT m.*, mem.firstname, mem.name " +
                "FROM Message m " +
                "INNER JOIN Member mem ON m.memberId = mem.id " +
                "WHERE m.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToMessageWithMember(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du message par ID :");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Message update(Message message) {
        String sql = "UPDATE Message SET content = ?, originalContent = ?, lastModifiedDate = ?, " +
                "isPin = ?, isHidden = ?, isModified = ?, isDeleted = ?, hiddenByMemberId = ? " +
                "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, message.getContent());
            stmt.setString(2, message.getOriginalContent());

            if (message.getLastModifiedDate() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(message.getLastModifiedDate()));
            } else {
                stmt.setTimestamp(3, null);
            }

            stmt.setBoolean(4, message.isPin());
            stmt.setBoolean(5, message.isHidden());
            stmt.setBoolean(6, message.isModified());
            stmt.setBoolean(7, message.isDeleted());

            if (message.getHiddenByMemberId() != null) {
                stmt.setLong(8, message.getHiddenByMemberId());
            } else {
                stmt.setNull(8, java.sql.Types.BIGINT);
            }

            stmt.setLong(9, message.getId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Message mis à jour : ID=" + message.getId());
                return message;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du message :");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void hideMessage(Long messageId) {
        String sql = "UPDATE Message SET isHidden = TRUE WHERE id = ?";
        executeUpdate(sql, messageId, "masqué");
    }

    @Override
    public void togglePin(Long messageId) {
        String sql = "UPDATE Message SET isPin = NOT isPin WHERE id = ?";
        executeUpdate(sql, messageId, "pin basculé");
    }

    @Override
    public void delete(Long messageId) {
        String sql = "DELETE FROM Message WHERE id = ?";
        executeUpdate(sql, messageId, "supprimé définitivement");
    }

    @Override
    public int countByDiscussionId(Long discussionId) {
        String sql = "SELECT COUNT(*) FROM Message WHERE discussionId = ? AND isDeleted = FALSE";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, discussionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des messages :");
            e.printStackTrace();
        }

        return 0;
    }

    // Méthodes utilitaires privées

    private List<Message> executeQueryWithJoin(String sql, Long discussionId) {
        List<Message> messages = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, discussionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(mapRowToMessageWithMember(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages :");
            e.printStackTrace();
        }

        return messages;
    }

    private void executeUpdate(String sql, Long messageId, String action) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, messageId);
            stmt.executeUpdate();
            System.out.println("Message " + action + " : ID=" + messageId);

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'action " + action + " :");
            e.printStackTrace();
        }
    }

    private Message mapRowToMessageWithMember(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setDiscussionId(rs.getLong("discussionId"));
        message.setMemberId(rs.getLong("memberId"));
        message.setContent(rs.getString("content"));

        // Nouveaux champs
        message.setOriginalContent(rs.getString("originalContent"));

        Timestamp lastModifiedTs = rs.getTimestamp("lastModifiedDate");
        if (lastModifiedTs != null) {
            message.setLastModifiedDate(lastModifiedTs.toLocalDateTime());
        }

        Timestamp timestamp = rs.getTimestamp("date");
        if (timestamp != null) {
            message.setDate(timestamp.toLocalDateTime());
        }

        message.setPin(rs.getBoolean("isPin"));
        message.setHidden(rs.getBoolean("isHidden"));
        message.setModified(rs.getBoolean("isModified"));
        message.setDeleted(rs.getBoolean("isDeleted"));

        long hiddenBy = rs.getLong("hiddenByMemberId");
        if (!rs.wasNull()) {
            message.setHiddenByMemberId(hiddenBy);
        }

        // Récupération des infos du membre via jointure
        message.setMemberFirstname(rs.getString("firstname"));
        message.setMemberName(rs.getString("name"));

        return message;
    }
}