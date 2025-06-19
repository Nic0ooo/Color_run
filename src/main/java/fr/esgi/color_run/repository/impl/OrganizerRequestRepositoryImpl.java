package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.OrganizerRequest;
import fr.esgi.color_run.business.RequestStatus;
import fr.esgi.color_run.repository.OrganizerRequestRepository;
import fr.esgi.color_run.util.Config;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrganizerRequestRepositoryImpl implements OrganizerRequestRepository {

    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public OrganizerRequestRepositoryImpl() {
        try {
            Class.forName("org.h2.Driver");
            System.out.println("✅ Driver H2 chargé pour OrganizerRequest");
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
        String sql = "CREATE TABLE IF NOT EXISTS OrganizerRequest (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "memberId BIGINT NOT NULL," +
                "motivation TEXT NOT NULL," +
                "existingAssociationId BIGINT," +
                "newAssociationData TEXT," + // JSON pour les données nouvelle association
                "requestDate TIMESTAMP NOT NULL," +
                "status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
                "adminComment TEXT," +
                "processedByAdminId BIGINT," +
                "processedDate TIMESTAMP" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'OrganizerRequest' vérifiée/créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table OrganizerRequest :");
            e.printStackTrace();
        }
    }

    @Override
    public OrganizerRequest save(OrganizerRequest request) {
        String sql = "INSERT INTO OrganizerRequest (memberId, motivation, existingAssociationId, newAssociationData, requestDate, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, request.getMemberId());
            ps.setString(2, request.getMotivation());
            if (request.getExistingAssociationId() != null) {
                ps.setLong(3, request.getExistingAssociationId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            // Pour les nouvelles associations, on pourrait sérialiser en JSON
            ps.setString(4, null); // TODO: Implémenter sérialisation JSON si nécessaire
            ps.setTimestamp(5, Timestamp.valueOf(request.getRequestDate()));
            ps.setString(6, request.getStatus().name());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    request.setId(rs.getLong(1));
                }
            }

            System.out.println("✅ Demande organisateur enregistrée : " + request.getId());
            return request;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la sauvegarde de la demande organisateur :");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<OrganizerRequest> findById(Long id) {
        String sql = "SELECT * FROM OrganizerRequest WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToOrganizerRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<OrganizerRequest> findAll() {
        List<OrganizerRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM OrganizerRequest ORDER BY requestDate DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(mapRowToOrganizerRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public List<OrganizerRequest> findPendingRequests() {
        return findByStatus(RequestStatus.PENDING);
    }

    @Override
    public List<OrganizerRequest> findByMemberId(Long memberId) {
        List<OrganizerRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM OrganizerRequest WHERE memberId = ? ORDER BY requestDate DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                requests.add(mapRowToOrganizerRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public List<OrganizerRequest> findByStatus(RequestStatus status) {
        List<OrganizerRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM OrganizerRequest WHERE status = ? ORDER BY requestDate DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                requests.add(mapRowToOrganizerRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public OrganizerRequest update(OrganizerRequest request) {
        String sql = "UPDATE OrganizerRequest SET status=?, adminComment=?, processedByAdminId=?, processedDate=? WHERE id=?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.getStatus().name());
            ps.setString(2, request.getAdminComment());
            if (request.getProcessedByAdminId() != null) {
                ps.setLong(3, request.getProcessedByAdminId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            if (request.getProcessedDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(request.getProcessedDate()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.setLong(5, request.getId());

            ps.executeUpdate();
            return request;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean hasActivePendingRequest(Long memberId) {
        String sql = "SELECT COUNT(*) FROM OrganizerRequest WHERE memberId = ? AND status = 'PENDING'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean deleteById(Long id) {
        String sql = "DELETE FROM OrganizerRequest WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private OrganizerRequest mapRowToOrganizerRequest(ResultSet rs) throws SQLException {
        OrganizerRequest request = new OrganizerRequest();
        request.setId(rs.getLong("id"));
        request.setMemberId(rs.getLong("memberId"));
        request.setMotivation(rs.getString("motivation"));

        Long existingAssocId = rs.getLong("existingAssociationId");
        if (!rs.wasNull()) {
            request.setExistingAssociationId(existingAssocId);
        }

        request.setRequestDate(rs.getTimestamp("requestDate").toLocalDateTime());
        request.setStatus(RequestStatus.valueOf(rs.getString("status")));
        request.setAdminComment(rs.getString("adminComment"));

        Long processedByAdminId = rs.getLong("processedByAdminId");
        if (!rs.wasNull()) {
            request.setProcessedByAdminId(processedByAdminId);
        }

        Timestamp processedDate = rs.getTimestamp("processedDate");
        if (processedDate != null) {
            request.setProcessedDate(processedDate.toLocalDateTime());
        }

        return request;
    }
}