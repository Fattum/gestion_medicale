package com.example.gestion_medicale;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestionnaire du pool de connexions HikariCP.
 *
 * <p>Cette classe est un singleton : le pool est créé une seule fois au démarrage
 * de l'application et partagé par tous les composants.</p>
 *
 * <p>La configuration est lue depuis {@code /database.properties} placé dans
 * {@code src/main/resources/}.</p>
 *
 * Usage :
 * <pre>
 *     try (Connection conn = DatabaseConnection.getConnection()) {
 *         // utiliser conn …
 *     }
 * </pre>
 */
public class DatabaseConnection {

    /** Source de données HikariCP (singleton). */
    private static HikariDataSource dataSource;

    /** Constructeur privé – classe utilitaire statique. */
    private DatabaseConnection() {}

    /**
     * Initialise le pool de connexions à partir de {@code database.properties}.
     * Appelée automatiquement lors du premier appel à {@link #getConnection()}.
     *
     * @throws RuntimeException si le fichier de propriétés est introuvable
     *                          ou si la connexion échoue.
     */
    private static synchronized void init() {
        if (dataSource != null) {
            return; // déjà initialisé
        }

        Properties props = new Properties();

        // Charger database.properties depuis le classpath
        try (InputStream is = DatabaseConnection.class
                .getResourceAsStream("/database.properties")) {

            if (is == null) {
                throw new RuntimeException(
                        "Fichier database.properties introuvable dans le classpath.");
            }
            props.load(is);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erreur de lecture de database.properties : " + e.getMessage(), e);
        }

        // Configurer HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));

        // Paramètres optionnels du pool
        String maxPool = props.getProperty("hikari.maximumPoolSize");
        if (maxPool != null) {
            config.setMaximumPoolSize(Integer.parseInt(maxPool));
        }

        String minIdle = props.getProperty("hikari.minimumIdle");
        if (minIdle != null) {
            config.setMinimumIdle(Integer.parseInt(minIdle));
        }

        String connTimeout = props.getProperty("hikari.connectionTimeout");
        if (connTimeout != null) {
            config.setConnectionTimeout(Long.parseLong(connTimeout));
        }

        String maxLifetime = props.getProperty("hikari.maxLifetime");
        if (maxLifetime != null) {
            config.setMaxLifetime(Long.parseLong(maxLifetime));
        }

        String keepalive = props.getProperty("hikari.keepaliveTime");
        if (keepalive != null) {
            config.setKeepaliveTime(Long.parseLong(keepalive));
        }

        // Requête de validation de la connexion
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("GestionMedicalePool");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Retourne une connexion disponible depuis le pool.
     *
     * <p>La connexion doit être fermée après usage (de préférence dans un
     * bloc try-with-resources) afin d'être rendue au pool.</p>
     *
     * @return une {@link Connection} active vers la base MySQL Railway
     * @throws SQLException si aucune connexion n'est disponible
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            init();
        }
        return dataSource.getConnection();
    }

    /**
     * Ferme proprement le pool de connexions.
     * Appeler cette méthode à l'arrêt de l'application.
     */
    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}