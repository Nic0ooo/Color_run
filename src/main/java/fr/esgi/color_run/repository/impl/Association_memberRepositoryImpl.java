
package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.Association_memberRepository;
import fr.esgi.color_run.repository.AssociationRepository;
import fr.esgi.color_run.repository.MemberRepository;
import fr.esgi.color_run.util.Config;
import fr.esgi.color_run.util.Mapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Association_memberRepositoryImpl implements Association_memberRepository {

    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    private final AssociationRepository associationRepository = new AssociationRepositoryImpl();
    private final MemberRepository memberRepository = new MemberRepositoryImpl();

    public Association_memberRepositoryImpl() {
        try {
            Class.forName("org.h2.Driver");
            System.out.println("✅ Driver H2 chargé pour AssociationMember");
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
        String sql = "CREATE TABLE IF NOT EXISTS AssociationMember (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "memberId BIGINT NOT NULL," +
                "associationId BIGINT NOT NULL," +
                "joinDate TIMESTAMP NOT NULL," +
                "FOREIGN KEY (memberId) REFERENCES member(id) ON DELETE CASCADE," +
                "FOREIGN KEY (associationId) REFERENCES association(id) ON DELETE CASCADE," +
                "UNIQUE(memberId)" + // Un organisateur = une association max
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'AssociationMember' vérifiée/créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table AssociationMember :");
            e.printStackTrace();
        }
    }

    @Override
    public void addOrganizerToAssociation(Long memberId, Long associationId) {
        // Supprimer l'ancienne association si elle existe
        // removeOrganizerFromAssociation(memberId, associationId);

        String sql = "INSERT INTO AssociationMember (memberId, associationId, joinDate) VALUES (?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, associationId);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            System.out.println("✅ Organisateur " + memberId + " ajouté à l'association " + associationId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout à l'association :");
            e.printStackTrace();
        }
    }

    @Override
    public void removeOrganizerFromAssociation(Long memberId, Long associationId) {
        String sql = "DELETE FROM AssociationMember WHERE memberId = ? AND associationId = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, associationId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Organisateur " + memberId + " retiré de son association " + associationId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du retrait de l'association :");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isOrganizerInAssociation(Long memberId, Long associationId) {
        String sql = "SELECT COUNT(*) FROM AssociationMember WHERE memberId = ? AND associationId = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, associationId);
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
    public Optional<Association> findAssociationByOrganizerId(Long memberId) {
        String sql = "SELECT a.* FROM association a " +
                "INNER JOIN AssociationMember am ON a.id = am.associationId " +
                "WHERE am.memberId = ? " +
                "ORDER BY a.name";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Association association = Mapper.mapRowToAssociation(rs);
                return Optional.of(association);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findOrganizersByAssociationId(Long associationId) {
        List<Member> organizers = new ArrayList<>();
        String sql = "SELECT m.* FROM member m " +
                "INNER JOIN AssociationMember am ON m.id = am.memberId " +
                "WHERE am.associationId = ? ORDER BY m.name, m.firstname";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, associationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                organizers.add(Mapper.mapRowToMember(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return organizers;
    }

    @Override
    public boolean organizerHasAssociation(Long memberId) {
        String sql = "SELECT COUNT(*) FROM AssociationMember WHERE memberId = ?";

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
    public int countOrganizersByAssociationId(Long associationId) {
        String sql = "SELECT COUNT(*) FROM AssociationMember " +
                "WHERE associationId = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, associationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Association> findAvailableAssociationsForMember(Long memberId) throws SQLException {
        String sql = " SELECT a.* FROM Association a " +
                "WHERE a.id NOT IN ( " +
                "SELECT am.associationId FROM AssociationMember am " +
                "WHERE am.memberId = ?) " +
                "ORDER BY a.name ";

        List<Association> associations = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    associations.add(Mapper.mapRowToAssociation(rs));
                }
            }
        }

        return associations;
    }

    @Override
    public List<AssociationMemberDetail> getAllAssociationMembers() {
        String sql = "SELECT am.id, am.memberId, am.associationId, am.joinDate, " +
                "m.firstname, m.name as memberName, m.email as memberEmail, m.role, " +
                "a.name as associationName, a.email as associationEmail, a.city " +
                "FROM AssociationMember am " +
                "INNER JOIN Member m ON am.memberId = m.id " +
                "INNER JOIN Association a ON am.associationId = a.id " +
                "ORDER BY a.name, m.firstname, m.name ";

        List<AssociationMemberDetail> details = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AssociationMemberDetail detail = new AssociationMemberDetail();

                detail.setId(rs.getLong("id"));
                detail.setMemberId(rs.getLong("memberId"));
                detail.setAssociationId(rs.getLong("associationId"));

                Timestamp joinDate = rs.getTimestamp("joinDate");
                if (joinDate != null) {
                    detail.setJoinDate(joinDate.toLocalDateTime());
                }

                detail.setMemberFirstName(rs.getString("firstname"));
                detail.setMemberName(rs.getString("memberName"));
                detail.setMemberEmail(rs.getString("memberEmail"));
                detail.setMemberRole(rs.getString("role"));
                detail.setAssociationName(rs.getString("associationName"));
                detail.setAssociationEmail(rs.getString("associationEmail"));
                detail.setAssociationCity(rs.getString("city"));

                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }

    @Override
    public void changeOrganizerAssociation(Long memberId, Long newAssociationId) {
        // Cette méthode fait la même chose que addOrganizerToAssociation
        // car celle-ci supprime déjà l'ancienne association
        addOrganizerToAssociation(memberId, newAssociationId);
    }

    // Classe interne pour les détails de relation membre-association
    public static class AssociationMemberDetail {
        private Long id;
        private Long memberId;
        private Long associationId;
        private LocalDateTime joinDate;
        private String memberFirstName;
        private String memberName;
        private String memberEmail;
        private String memberRole;
        private String associationName;
        private String associationEmail;
        private String associationCity;

        // Constructeurs
        public AssociationMemberDetail() {}

        // Getters et setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }

        public Long getAssociationId() { return associationId; }
        public void setAssociationId(Long associationId) { this.associationId = associationId; }

        public LocalDateTime getJoinDate() { return joinDate; }
        public void setJoinDate(LocalDateTime joinDate) { this.joinDate = joinDate; }

        public String getMemberFirstName() { return memberFirstName; }
        public void setMemberFirstName(String memberFirstName) { this.memberFirstName = memberFirstName; }

        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }

        public String getMemberEmail() { return memberEmail; }
        public void setMemberEmail(String memberEmail) { this.memberEmail = memberEmail; }

        public String getMemberRole() { return memberRole; }
        public void setMemberRole(String memberRole) { this.memberRole = memberRole; }

        public String getAssociationName() { return associationName; }
        public void setAssociationName(String associationName) { this.associationName = associationName; }

        public String getAssociationEmail() { return associationEmail; }
        public void setAssociationEmail(String associationEmail) { this.associationEmail = associationEmail; }

        public String getAssociationCity() { return associationCity; }
        public void setAssociationCity(String associationCity) { this.associationCity = associationCity; }

        public String getFullMemberName() {
            return (memberFirstName != null ? memberFirstName + " " : "") +
                    (memberName != null ? memberName : "");
        }
    }
}