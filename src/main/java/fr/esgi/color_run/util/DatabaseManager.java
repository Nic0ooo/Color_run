package fr.esgi.color_run.util;

import fr.esgi.color_run.util.Config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Gestionnaire singleton pour la base de données avec pool de connexions
 * et cache des vérifications de tables
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static final Object lock = new Object();

    private final DataSource dataSource;
    private final Set<String> verifiedTables = new HashSet<>();
    private static boolean driverLoaded = false;
    private static boolean isTestMode = false;

    // Méthode pour activer le mode test et réinitialiser l'instance
    public static void enableTestMode() {
        synchronized (lock) {
            isTestMode = true;
            instance = null; // Force réinitialisation lors du prochain appel à getInstance()
        }
    }

    // Méthode pour désactiver le mode test et réinitialiser l'instance
    public static void disableTestMode() {
        synchronized (lock) {
            isTestMode = false;
            instance = null; // Force réinitialisation lors du prochain appel à getInstance()
        }
    }

    private DatabaseManager() {
        loadDriver();
        this.dataSource = createDataSource();
        System.out.println("✅ DatabaseManager initialisé avec pool de connexions" +
                           (isTestMode ? " (MODE TEST)" : ""));
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private void loadDriver() {
        if (!driverLoaded) {
            try {
                Class.forName("org.h2.Driver");
                driverLoaded = true;
                System.out.println("✅ Driver H2 chargé (singleton)");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver H2 introuvable !");
                throw new RuntimeException(e);
            }
        }
    }

    private DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        String dbPath;

        if (isTestMode) {
            // Utiliser le chemin de la base de données de test
            dbPath = Config.get("db.path") + "_test";
            System.out.println("🔍 Utilisation de la base de données de test : " + dbPath);
        } else {
            // Utiliser le chemin de la base de données normale
            dbPath = Config.get("db.path");
        }

        config.setJdbcUrl("jdbc:h2:" + dbPath + ";AUTO_SERVER=TRUE");
        config.setUsername("sa");
        config.setPassword("");

        // Configuration du pool
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        return new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Vérifie et crée une table seulement si elle n'a pas déjà été vérifiée
     */
    public void ensureTableExists(String tableName, String createSql) {
        if (verifiedTables.contains(tableName)) {
            return; // Table déjà vérifiée
        }

        synchronized (verifiedTables) {
            if (verifiedTables.contains(tableName)) {
                return; // Double-check
            }

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(createSql);
                verifiedTables.add(tableName);
                System.out.println("✅ Table '" + tableName + "' vérifiée/créée (cache)");
            } catch (SQLException e) {
                System.err.println("❌ Erreur création table " + tableName + " :");
                e.printStackTrace();
            }
        }
    }

    /**
     * Test de connexion optimisé
     */
    public void testConnection() {
        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Pool de connexions opérationnel");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur test connexion pool:");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            System.out.println("✅ Pool de connexions fermé");
        }
    }
}