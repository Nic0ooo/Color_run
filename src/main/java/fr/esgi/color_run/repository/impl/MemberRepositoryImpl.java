package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.MemberRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import fr.esgi.color_run.util.Config;

public class MemberRepositoryImpl implements MemberRepository {

    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public MemberRepositoryImpl() {
        try {
            // Obligatoire pour que Tomcat charge le driver H2
            Class.forName("org.h2.Driver");
            System.out.println("✅ Driver H2 chargé");
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
        String sql = "CREATE TABLE IF NOT EXISTS member (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
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

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'member' vérifiée/créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table :");
            e.printStackTrace();
        }
    }

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO member (name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                return Optional.of(mapRowToMember(rs));
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
                return Optional.of(mapRowToMember(rs));
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
                members.add(mapRowToMember(rs));
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
        String sql = "UPDATE member SET name=?, firstname=?, email=?, password=?, phoneNumber=?, address=?, city=?, zipCode=?, positionLatitude=?, positionLongitude=? " +
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
            ps.setLong(11, member.getId());

            ps.executeUpdate();
            return member;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

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
}
