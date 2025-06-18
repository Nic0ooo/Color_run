package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.repository.AssociationRepository;
import fr.esgi.color_run.util.Config;
import fr.esgi.color_run.util.Mapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                "zipCode INT," +
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
        String sql = "SELECT * FROM association";

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
            } else {
                System.out.println("❌ Aucune association trouvée avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'association par ID :");
            e.printStackTrace();
        }
        return association;
    }

}
