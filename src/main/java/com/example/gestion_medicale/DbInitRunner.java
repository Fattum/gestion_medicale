package com.example.gestion_medicale;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Standalone runner to initialize the database without launching the JavaFX UI.
 * Run with: mvn compile exec:java -Dexec.mainClass="com.example.gestion_medicale.DbInitRunner"
 */
public class DbInitRunner {

    public static void main(String[] args) {
        System.out.println("=== Initialisation de la base de données ===");
        DatabaseInitializer.initialize();

        // Verify the tables were created
        System.out.println("\n=== Vérification des tables ===");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("Tables présentes:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
            }

            rs = stmt.executeQuery("SELECT id, nom, role FROM Utilisateur");
            System.out.println("\nUtilisateurs:");
            while (rs.next()) {
                System.out.printf("  id=%d  nom=%s  role=%s%n",
                        rs.getInt("id"), rs.getString("nom"), rs.getString("role"));
            }

            rs = stmt.executeQuery("SELECT id, nom FROM Specialite");
            System.out.println("\nSpécialités:");
            while (rs.next()) {
                System.out.printf("  id=%d  nom=%s%n",
                        rs.getInt("id"), rs.getString("nom"));
            }

        } catch (Exception e) {
            System.err.println("Erreur de vérification: " + e.getMessage());
        }

        System.out.println("\nTerminé.");
        DatabaseConnection.close();
        System.exit(0);
    }
}