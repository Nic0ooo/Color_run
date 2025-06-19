package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.repository.AssociationRepository;
import fr.esgi.color_run.util.Config;
import fr.esgi.color_run.util.Mapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssociationRepositoryImpl implements AssociationRepository {

    private final String jdbcUrl = "jdbc:h2:" + Config.get("db.path") + ";AUTO_SERVER=TRUE";
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public AssociationRepositoryImpl() {
        try {
            // Obligatoire pour que Tomcat charge le driver H2
            Class.forName("org.h2.Driver");
            System.out.println("✅ Driver H2 chargé");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver H2 introuvable !");
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
                System.out.println("✅ Connexion à la base de données réussie !");
            } else {
                System.out.println("❌ Échec de la connexion à la base de données.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la tentative de connexion à la base de données :");
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS association (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(255) NOT NULL," +
                "description VARCHAR(255)," +
                "websiteLink VARCHAR(255)," +
                "logoPath VARCHAR(255)," +
                "email VARCHAR(255) UNIQUE," +
                "phoneNumber VARCHAR(20)," +
                "address VARCHAR(255)," +
                "city VARCHAR(255)," +
                "zipCode INT" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'association' vérifiée/créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table :");
            e.printStackTrace();
        }
    }


    @Override
    public List<Association> findAll() {
        List<Association> associations = new ArrayList<>();
        System.out.println("AssociationRepositoryImpl: findAll() - Exécution de la requête pour récupérer toutes les associations.");
        String sql = "SELECT * FROM association ORDER BY name";

        try (Connection connection = getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                associations.add(Mapper.mapRowToAssociation(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des associations :");
            e.printStackTrace();
        }
        if (associations.isEmpty()) {
            System.out.println("❌ Aucune association trouvée dans la base de données.");
        } else {
            System.out.println("✅ " + associations.size() + " associations trouvées.");
        }
        return associations;
    }

    @Override
    public Association findById(Long id) {
        Association association = null;
        String sql = "SELECT * FROM association WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                association = Mapper.mapRowToAssociation(resultSet);
                System.out.println("✅ Association trouvée : " + association.getName());
                return association;
            } else {
                System.out.println("❌ Aucune association trouvée avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'association par ID :");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Association save(Association association) {
        String sql = "INSERT INTO association (name, description, website_link, logo_path, email, phone_number, address, city, zip_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, association.getName());
            ps.setString(2, association.getDescription());
            ps.setString(3, association.getWebsiteLink());
            ps.setString(4, association.getLogoPath());
            ps.setString(5, association.getEmail());
            ps.setString(6, association.getPhoneNumber());
            ps.setString(7, association.getAddress());
            ps.setString(8, association.getCity());
            ps.setInt(9, association.getZipCode() != null ? association.getZipCode() : 0);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    association.setId(rs.getLong(1));
                }
            }

            System.out.println("✅ Association enregistrée : " + association.getName());
            return association;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la sauvegarde de l'association :");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Association update(Association association) {
        String sql = "UPDATE association SET name=?, description=?, website_link=?, logo_path=?, email=?, phone_number=?, address=?, city=?, zip_code=? " +
                "WHERE id=?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, association.getName());
            ps.setString(2, association.getDescription());
            ps.setString(3, association.getWebsiteLink());
            ps.setString(4, association.getLogoPath());
            ps.setString(5, association.getEmail());
            ps.setString(6, association.getPhoneNumber());
            ps.setString(7, association.getAddress());
            ps.setString(8, association.getCity());
            ps.setInt(9, association.getZipCode() != null ? association.getZipCode() : 0);
            ps.setLong(10, association.getId());

            ps.executeUpdate();
            System.out.println("✅ Association mise à jour : " + association.getName());
            return association;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'association :");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM association WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Association supprimée : ID " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'association :");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Association> findByNameContaining(String name) {
        List<Association> associations = new ArrayList<>();
        String sql = "SELECT * FROM association WHERE LOWER(name) LIKE LOWER(?) ORDER BY name";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                associations.add(Mapper.mapRowToAssociation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return associations;
    }

    @Override
    public Optional<Association> findByName(String name) {
        String sql = "SELECT * FROM association WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(Mapper.mapRowToAssociation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Association> findByCity(String city) {
        List<Association> associations = new ArrayList<>();
        String sql = "SELECT * FROM association WHERE LOWER(city) = LOWER(?) ORDER BY name";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, city);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                associations.add(Mapper.mapRowToAssociation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return associations;
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM association WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

