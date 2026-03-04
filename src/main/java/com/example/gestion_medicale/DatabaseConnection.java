package com.example.gestion_medicale;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
public class DatabaseConnection {
    private static HikariDataSource dataSource;
    private DatabaseConnection() {}
    private static synchronized void init() {
        if (dataSource != null) {
            return; 
        }

        Properties props = new Properties();
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
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
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
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("GestionMedicalePool");

        dataSource = new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            init();
        }
        return dataSource.getConnection();
    }
    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}