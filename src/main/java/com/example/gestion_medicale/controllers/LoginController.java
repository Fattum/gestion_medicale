package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.SessionManager;
import com.example.gestion_medicale.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtNom;
    @FXML private PasswordField txtMotDePasse;
    @FXML private Label lblErreur;

    @FXML
    private void handleLogin() {
        String nom = txtNom.getText().trim();
        String motDePasse = txtMotDePasse.getText().trim();

        if (nom.isEmpty() || motDePasse.isEmpty()) {
            lblErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, nom, motDePasse, role FROM Utilisateur WHERE nom = ? AND motDePasse = ?")) {
            stmt.setString(1, nom);
            stmt.setString(2, motDePasse);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("motDePasse"),
                        rs.getString("role")
                );
                SessionManager.getInstance().setCurrentUser(user);

                Stage stage = (Stage) txtNom.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/gestion_medicale/MainView.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(
                        getClass().getResource("/com/example/gestion_medicale/styles.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Gestion Médicale - " + user.getNom());
                stage.setMaximized(true);
            } else {
                lblErreur.setText("Nom d'utilisateur ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblErreur.setText("Erreur de connexion à la base de données.");
        }
    }
}