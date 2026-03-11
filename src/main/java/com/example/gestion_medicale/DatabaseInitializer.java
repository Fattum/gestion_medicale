package com.example.gestion_medicale;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void initialize() {
        try (InputStream is = DatabaseInitializer.class.getResourceAsStream("/init_database.sql")) {
            if (is == null) {
                System.err.println("Fichier SQL introuvable");
                return;
            }
            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            String[] statements = sql.split(";");
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String s : statements) {
                    // Strip comment lines from each statement before executing
                    String withoutComments = Arrays.stream(s.split("\n"))
                            .filter(line -> !line.trim().startsWith("--"))
                            .collect(Collectors.joining("\n"))
                            .trim();
                    if (!withoutComments.isEmpty()) {
                        stmt.execute(withoutComments);
                    }
                }
            }
            System.out.println("Base de données initialisée avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }
}