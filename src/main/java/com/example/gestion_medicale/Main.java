package com.example.gestion_medicale;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/gestion_medicale/login.fxml"));
        Scene scene = new Scene(loader.load(), 450, 350);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/gestion_medicale/styles.css").toExternalForm());
        stage.setTitle("Gestion Médicale - Connexion");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        // Initialize database on startup
        DatabaseInitializer.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::close));
        Runtime.getRuntime().addShutdownHook(new Thread(AppExecutors::shutdown));
        launch(args);
    }
}