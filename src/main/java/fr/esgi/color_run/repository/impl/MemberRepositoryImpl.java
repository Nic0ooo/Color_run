package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.repository.MemberRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import fr.esgi.color_run.util.DatabaseManager;
import fr.esgi.color_run.util.Mapper;

public class MemberRepositoryImpl implements MemberRepository {

    private final DatabaseManager dbManager;

    public MemberRepositoryImpl() {
        this.dbManager = DatabaseManager.getInstance();
        ensureTableExists();
    }

    private Connection getConnection() throws SQLException {
        return dbManager.getConnection();
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS member (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "role VARCHAR(20) NOT NULL DEFAULT 'RUNNER'," +
                "name VARCHAR(255)," +
                "firstname VARCHAR(255)," +
                "email VARCHAR(255) UNIQUE," +
                "password VARCHAR(255)," +
                "phoneNumber VARCHAR(20)," +
                "address VARCHAR(255)," +
                "city VARCHAR(255)," +
                "zipCode INT," +
                "positionLatitude DOUBLE," +
                "positionLongitude DOUBLE" +
                ");";

        dbManager.ensureTableExists("member", sql);
    }

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO member (name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, member.getName());
            ps.setString(2, member.getFirstname());
            ps.setString(3, member.getEmail());
            ps.setString(4, member.getPassword());
            ps.setString(5, member.getPhoneNumber());
            ps.setString(6, member.getAddress());
            ps.setString(7, member.getCity());
            ps.setInt(8, member.getZipCode() != null ? member.getZipCode() : 0);
            ps.setDouble(9, member.getPositionLatitude());
            ps.setDouble(10, member.getPositionLongitude());
            ps.setString(11, member.getRole().name());



            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    member.setId(rs.getLong(1));
                }
            }

            System.out.println("Membre enregistré : " + member.getEmail());
            return member;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du membre :");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(Mapper.mapRowToMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(Mapper.mapRowToMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(Mapper.mapRowToMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    @Override
    public Boolean deleteById(Long id) {
        String sql = "DELETE FROM member WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Member update(Member member) {
        String sql = "UPDATE member SET name=?, firstname=?, email=?, password=?, phoneNumber=?, address=?, city=?, zipCode=?, positionLatitude=?, positionLongitude=?, role=? " +
                "WHERE id=?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getFirstname());
            ps.setString(3, member.getEmail());
            ps.setString(4, member.getPassword());
            ps.setString(5, member.getPhoneNumber());
            ps.setString(6, member.getAddress());
            ps.setString(7, member.getCity());
            ps.setInt(8, member.getZipCode() != null ? member.getZipCode() : 0);
            ps.setDouble(9, member.getPositionLatitude());
            ps.setDouble(10, member.getPositionLongitude());
            ps.setString(11, member.getRole().name());
            ps.setLong(12, member.getId());

            ps.executeUpdate();
            return member;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // à supprimer -> laissé pour ne pas caisser après merge, mappage se fait maintenant via /util/Mapper pour être accesible partout
    private Member mapRowToMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getLong("id"));
        m.setName(rs.getString("name"));
        m.setFirstname(rs.getString("firstname"));
        m.setEmail(rs.getString("email"));
        m.setPassword(rs.getString("password"));
        m.setPhoneNumber(rs.getString("phoneNumber"));
        m.setAddress(rs.getString("address"));
        m.setCity(rs.getString("city"));
        m.setZipCode(rs.getInt("zipCode"));
        m.setPositionLatitude(rs.getDouble("positionLatitude"));
        m.setPositionLongitude(rs.getDouble("positionLongitude"));
        return m;
    }

    @Override
    public void updatePasswordByEmail(String email, String password) {
        String sql = "UPDATE member SET password = ? WHERE email = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.executeUpdate();

            System.out.println("✅ Mot de passe mis à jour pour : " + email);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du mot de passe pour " + email);
            e.printStackTrace();
        }
    }

    @Override
    public List<Member> findByRole(Role role) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE role = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                members.add(Mapper.mapRowToMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    @Override
    public void updateMemberRole(Long memberId, Role newRole) {
        String sql = "UPDATE member SET role = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole.name());
            ps.setLong(2, memberId);
            ps.executeUpdate();
            System.out.println("✅ Rôle mis à jour pour le membre " + memberId + " : " + newRole);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du rôle :");
            e.printStackTrace();
        }
    }

    @Override
    public List<Member> findOrganizersByAssociationId(Long associationId) {
        List<Member> organizers = new ArrayList<>();
        String sql = "SELECT m.* FROM member m " +
                "INNER JOIN association_member am ON m.id = am.member_id " +
                "WHERE am.association_id = ? AND m.role = 'ORGANIZER'";
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
    public int countOrganizersByAssociationId(Long associationId) {
        String sql = "SELECT COUNT(*) FROM member m " +
                "INNER JOIN association_member am ON m.id = am.member_id " +
                "WHERE am.association_id = ? AND m.role = 'ORGANIZER'";
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

}
