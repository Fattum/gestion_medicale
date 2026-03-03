package com.example.gestion_medicale;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée JavaFX de l'application Gestion Médicale.
 *
 * <p>Au démarrage :</p>
 * <ol>
 *   <li>Les tables MySQL sont créées automatiquement ({@link DatabaseInitializer})</li>
 *   <li>La fenêtre de connexion ({@code login.fxml}) est affichée</li>
 * </ol>
 * <p>A la fermeture, le pool HikariCP est proprement libéré.</p>
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        // ── 1. Initialisation automatique de la base de données ──────────────
        //    Crée les tables users/doctors et insère les comptes par défaut
        //    si ils n'existent pas encore.
        DatabaseInitializer.initialize();

        // ── 2. Chargement de la fenêtre de connexion ─────────────────────────
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/example/gestion_medicale/login.fxml"));

        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Gestion Médicale – Connexion");
        primaryStage.setScene(scene);
        primaryStage.setWidth(520);
        primaryStage.setHeight(480);
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(440);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Appelée par le runtime JavaFX à la fermeture de l'application.
     * Ferme proprement le pool de connexions HikariCP.
     */
    @Override
    public void stop() {
        DatabaseConnection.close();
    }

    /**
     * Point d'entrée JVM – délègue à {@link Application#launch(String...)}.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        launch(args);
    }
}