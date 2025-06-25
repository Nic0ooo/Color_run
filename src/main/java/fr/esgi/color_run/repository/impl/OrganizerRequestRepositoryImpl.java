package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.OrganizerRequest;
import fr.esgi.color_run.business.RequestStatus;
import fr.esgi.color_run.business.RequestType;
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
        // CORRECTION: Utiliser le bon nom de colonne (tout en minuscules)
        String sql = "INSERT INTO OrganizerRequest (memberId, motivation, existingAssociationId, newassociationdata, requestDate, status, adminComment, processedByAdminId, processedDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, request.getMemberId());
            ps.setString(2, request.getMotivation());

            if (request.getExistingAssociationId() != null) {
                ps.setLong(3, request.getExistingAssociationId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            // Construire le JSON pour newAssociationData si une nouvelle association est demandée
            String newAssociationJson = null;
            if (request.hasNewAssociation()) {
                newAssociationJson = buildNewAssociationJson(request);
                System.out.println("🔍 JSON généré: " + newAssociationJson);
            }
            ps.setString(4, newAssociationJson);

            ps.setTimestamp(5, Timestamp.valueOf(request.getRequestDate()));
            ps.setString(6, request.getStatus().name());
            ps.setString(7, request.getAdminComment());

            if (request.getProcessedByAdminId() != null) {
                ps.setLong(8, request.getProcessedByAdminId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }

            if (request.getProcessedDate() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(request.getProcessedDate()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }

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
            throw new RuntimeException("Erreur lors de la sauvegarde", e);
        }
    }

    @Override
    public Optional<OrganizerRequest> findById(Long id) {
        String sql = "SELECT oreq.*, a.name as associationName " +
                "FROM OrganizerRequest oreq " +
                "LEFT JOIN Association a ON oreq.existingAssociationId = a.id " +
                "WHERE oreq.id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToOrganizerRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<OrganizerRequest> findAll() {
        List<OrganizerRequest> requests = new ArrayList<>();
        String sql = "SELECT oreq.*, a.name as associationName FROM OrganizerRequest oreq " +
                "LEFT JOIN Association a ON oreq.existingAssociationId = a.id " +
                "ORDER BY oreq.requestDate DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(mapResultSetToOrganizerRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public List<OrganizerRequest> findByStatus(RequestStatus status) {
        String sql = "SELECT oreq.*, a.name as associationName " +
                "FROM OrganizerRequest oreq " +
                "LEFT JOIN Association a ON oreq.existingAssociationId = a.id " +
                "WHERE oreq.status = ? " +
                "ORDER BY oreq.requestDate DESC";

        List<OrganizerRequest> requests = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapResultSetToOrganizerRequest(resultSet));
                }
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
        String sql = "SELECT oreq.*, a.name as associationName FROM OrganizerRequest oreq " +
                "LEFT JOIN Association a ON oreq.existingAssociationId = a.id " +
                "WHERE oreq.memberId = ? ORDER BY oreq.requestDate DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                requests.add(mapResultSetToOrganizerRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public boolean hasActivePendingRequest(Long memberId) {
        String sql = "SELECT COUNT(*) FROM OrganizerRequest WHERE memberId = ? AND status = 'PENDING'";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public OrganizerRequest update(OrganizerRequest request) {
        // CORRECTION: Utiliser le bon nom de colonne
        String sql = "UPDATE OrganizerRequest SET motivation = ?, existingAssociationId = ?, newassociationdata = ?, status=?, adminComment=?, processedByAdminId=?, processedDate=? WHERE id=?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getMotivation());

            if (request.getExistingAssociationId() != null) {
                ps.setLong(2, request.getExistingAssociationId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            String newAssociationJson = null;
            if (request.hasNewAssociation()) {
                newAssociationJson = buildNewAssociationJson(request);
            }
            ps.setString(3, newAssociationJson);

            ps.setString(4, request.getStatus().name());
            ps.setString(5, request.getAdminComment());

            if (request.getProcessedByAdminId() != null) {
                ps.setLong(6, request.getProcessedByAdminId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            if (request.getProcessedDate() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(request.getProcessedDate()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }

            ps.setLong(8, request.getId());

            ps.executeUpdate();
            return request;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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

    private OrganizerRequest mapResultSetToOrganizerRequest(ResultSet resultSet) throws SQLException {
        OrganizerRequest request = new OrganizerRequest();

        request.setId(resultSet.getLong("id"));
        request.setMemberId(resultSet.getLong("memberId"));
        request.setMotivation(resultSet.getString("motivation"));

        // Association existante
        Long existingAssocId = resultSet.getLong("existingAssociationId");
        if (!resultSet.wasNull()) {
            request.setExistingAssociationId(existingAssocId);
            request.setExistingAssociationName(resultSet.getString("associationName"));
        }

        // Nouvelle association (parsing du JSON) - CORRECTION: nom de colonne
        String newAssociationData = resultSet.getString("newassociationdata");
        if (newAssociationData != null && !newAssociationData.trim().isEmpty()) {
            parseNewAssociationJson(request, newAssociationData);
        }

        // Dates
        Timestamp requestDate = resultSet.getTimestamp("requestDate");
        if (requestDate != null) {
            request.setRequestDate(requestDate.toLocalDateTime());
        }

        Timestamp processedDate = resultSet.getTimestamp("processedDate");
        if (processedDate != null) {
            request.setProcessedDate(processedDate.toLocalDateTime());
        }

        // Status
        String statusStr = resultSet.getString("status");
        if (statusStr != null) {
            try {
                request.setStatus(RequestStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                request.setStatus(RequestStatus.PENDING);
            }
        }

        // Détermine le type de demande basé sur les données
        if (request.hasNewAssociation()) {
            request.setRequestType(RequestType.CREATE_ASSOCIATION);
        } else if (request.getExistingAssociationId() != null) {
            request.setRequestType(RequestType.JOIN_ASSOCIATION);
        } else {
            request.setRequestType(RequestType.BECOME_ORGANIZER);
        }

        request.setAdminComment(resultSet.getString("adminComment"));

        Long processedByAdminId = resultSet.getLong("processedByAdminId");
        if (!resultSet.wasNull()) {
            request.setProcessedByAdminId(processedByAdminId);
        }

        return request;
    }

    private String buildNewAssociationJson(OrganizerRequest request) {
        StringBuilder json = new StringBuilder("{");

        if (request.getNewAssociationName() != null) {
            json.append("\"name\":\"").append(escapeJson(request.getNewAssociationName())).append("\",");
        }
        if (request.getNewAssociationEmail() != null) {
            json.append("\"email\":\"").append(escapeJson(request.getNewAssociationEmail())).append("\",");
        }
        if (request.getNewAssociationDescription() != null) {
            json.append("\"description\":\"").append(escapeJson(request.getNewAssociationDescription())).append("\",");
        }
        if (request.getNewAssociationWebsiteLink() != null) {
            json.append("\"websiteLink\":\"").append(escapeJson(request.getNewAssociationWebsiteLink())).append("\",");
        }
        if (request.getNewAssociationPhone() != null) {
            json.append("\"phone\":\"").append(escapeJson(request.getNewAssociationPhone())).append("\",");
        }
        if (request.getNewAssociationAddress() != null) {
            json.append("\"address\":\"").append(escapeJson(request.getNewAssociationAddress())).append("\",");
        }
        if (request.getNewAssociationZipCode() != null) {
            json.append("\"zipCode\":\"").append(escapeJson(request.getNewAssociationZipCode())).append("\",");
        }
        if (request.getNewAssociationCity() != null) {
            json.append("\"city\":\"").append(escapeJson(request.getNewAssociationCity())).append("\",");
        }

        // Supprimer la dernière virgule
        if (json.length() > 1 && json.charAt(json.length() - 1) == ',') {
            json.setLength(json.length() - 1);
        }

        json.append("}");
        return json.toString();
    }

    private void parseNewAssociationJson(OrganizerRequest request, String json) {
        if (json == null || json.trim().isEmpty()) return;

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return;

        try {
            String content = json.substring(1, json.length() - 1);
            String[] pairs = content.split(",");

            for (String pair : pairs) {
                if (pair.contains(":")) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");

                        switch (key) {
                            case "name":
                                request.setNewAssociationName(value);
                                break;
                            case "email":
                                request.setNewAssociationEmail(value);
                                break;
                            case "description":
                                request.setNewAssociationDescription(value);
                                break;
                            case "websiteLink":
                                request.setNewAssociationWebsiteLink(value);
                                break;
                            case "phone":
                                request.setNewAssociationPhone(value);
                                break;
                            case "address":
                                request.setNewAssociationAddress(value);
                                break;
                            case "zipCode":
                                request.setNewAssociationZipCode(value);
                                break;
                            case "city":
                                request.setNewAssociationCity(value);
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du parsing JSON: " + e.getMessage());
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}