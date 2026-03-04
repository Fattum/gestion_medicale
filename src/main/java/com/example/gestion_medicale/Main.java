package com.example.gestion_medicale;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        DatabaseInitializer.initialize();
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
    @Override
    public void stop() {
        DatabaseConnection.close();
    }
    public static void main(String[] args) {
        launch(args);
    }
}