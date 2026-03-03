package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Contrôleur de la page de connexion - Design moderne
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         messageLabel;
    @FXML private Button        loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("⚠ Veuillez saisir votre nom d'utilisateur et mot de passe.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours...");

        try {
            User user = authenticate(username, password);
            if (user != null) {
                // Block doctor role
                if ("doctor".equalsIgnoreCase(user.getRole())) {
                    showError("❌ Accès refusé. Seuls les administrateurs et secrétaires peuvent se connecter.");
                    loginButton.setDisable(false);
                    loginButton.setText("Se connecter →");
                    passwordField.clear();
                    return;
                }
                // Pass the logged-in user to MainController
                MainController.setCurrentUser(user);

                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gestion_medicale/MainView.fxml")
                );
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("MediAdmin — Gestion Médicale");
                stage.setWidth(1150);
                stage.setHeight(720);
                stage.centerOnScreen();
            } else {
                showError("❌ Nom d'utilisateur ou mot de passe incorrect.");
                loginButton.setDisable(false);
                loginButton.setText("Se connecter →");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (IOException e) {
            showError("❌ Erreur lors du chargement de l'interface principale.");
            loginButton.setDisable(false);
            loginButton.setText("Se connecter →");
            e.printStackTrace();
        } catch (Exception e) {
            showError("❌ Erreur de connexion à la base de données : " + e.getMessage());
            loginButton.setDisable(false);
            loginButton.setText("Se connecter →");
            e.printStackTrace();
        }
    }

    /**
     * Vérifie les credentials en base de données
     */
    private User authenticate(String username, String password) throws Exception {
        String sql = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    "",
                    rs.getString("role")
                );
            }
        }
        return null;
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setVisible(true);
        messageLabel.setStyle(
            "-fx-text-fill: #ef4444; -fx-font-size: 13px;"
        );
    }
}