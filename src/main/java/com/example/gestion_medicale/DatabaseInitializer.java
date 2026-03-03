package com.example.gestion_medicale;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initialise automatiquement les tables de la base de données au démarrage.
 *
 * <p>Crée les tables {@code users} et {@code doctors} si elles n'existent pas
 * encore, et insère les comptes par défaut (admin, secretaire, docteur).</p>
 *
 * <p>Appelée une seule fois depuis {@link Main#start(javafx.stage.Stage)}.</p>
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {}

    /**
     * Exécute les instructions DDL et les données initiales.
     * Utilise {@code CREATE TABLE IF NOT EXISTS} et {@code INSERT IGNORE}
     * pour être idempotente (peut être appelée plusieurs fois sans effet).
     */
    public static void initialize() {

        // ── Création de la table users ────────────────────────────────────
        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id       INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role     ENUM('admin','secretary','doctor') NOT NULL DEFAULT 'secretary'
                )
                """;

        // ── Création de la table doctors ──────────────────────────────────
        String createDoctors = """
                CREATE TABLE IF NOT EXISTS doctors (
                    id         INT AUTO_INCREMENT PRIMARY KEY,
                    nom        VARCHAR(100) NOT NULL,
                    prenom     VARCHAR(100) NOT NULL,
                    specialite VARCHAR(150),
                    user_id    INT,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
                )
                """;

        // ── Comptes par défaut ────────────────────────────────────────────
        // admin / admin123
        String insertAdmin = """
                INSERT IGNORE INTO users (username, password, role)
                VALUES ('admin', 'admin123', 'admin')
                """;

        // secretaire / secret123
        String insertSecretaire = """
                INSERT IGNORE INTO users (username, password, role)
                VALUES ('secretaire', 'secret123', 'secretary')
                """;

        // docteur / doctor123
        String insertDocteur = """
                INSERT IGNORE INTO users (username, password, role)
                VALUES ('docteur', 'doctor123', 'doctor')
                """;

        // ── Médecins d'exemple ────────────────────────────────────────────
        String insertDoctors = """
                INSERT IGNORE INTO doctors (nom, prenom, specialite, user_id)
                VALUES ('Martin', 'Sophie', 'Cardiologue', NULL),
                       ('Bernard', 'Pierre', 'Généraliste', NULL),
                       ('Durand', 'Marie', 'Pédiatre', NULL)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createUsers);
            stmt.executeUpdate(createDoctors);
            stmt.executeUpdate(insertAdmin);
            stmt.executeUpdate(insertSecretaire);
            stmt.executeUpdate(insertDocteur);
            stmt.executeUpdate(insertDoctors);

            System.out.println("[DB] Initialisation terminée avec succès.");

        } catch (SQLException e) {
            System.err.println("[DB] Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}