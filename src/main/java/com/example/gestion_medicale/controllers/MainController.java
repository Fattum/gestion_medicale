package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

/**
 * Contrôleur principal : gère la sidebar et le contenu dynamique
 */
public class MainController implements Initializable {

    // ----- Sidebar buttons -----
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnDoctors;

    // ----- Top bar -----
    @FXML private Label pageTitle;
    @FXML private Label topUserBadge;

    // ----- User info -----
    @FXML private Label loggedUserLabel;
    @FXML private Label loggedRoleLabel;
    @FXML private Label userInitialLabel;
    @FXML private Label welcomeLabel;

    // ----- Stats -----
    @FXML private Label statUsers;
    @FXML private Label statDoctors;

    // ----- Panes -----
    @FXML private VBox dashboardPane;
    @FXML private VBox usersPane;
    @FXML private VBox doctorsPane;

    // Logged-in user
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set user info in sidebar
        if (currentUser != null) {
            loggedUserLabel.setText(currentUser.getUsername());
            loggedRoleLabel.setText(currentUser.getRole());
            userInitialLabel.setText(currentUser.getUsername().substring(0, 1).toUpperCase());
            topUserBadge.setText("● " + currentUser.getUsername());
            welcomeLabel.setText("Bienvenue, " + currentUser.getUsername() + "! 👋");
        }
        loadStats();
        showDashboard();
    }

    /** Load counts from DB for stat cards */
    private void loadStats() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) statUsers.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM doctors");
            if (rs.next()) statDoctors.setText(String.valueOf(rs.getInt(1)));

        } catch (Exception e) {
            statUsers.setText("—");
            statDoctors.setText("—");
        }
    }

    // ===== SIDEBAR NAVIGATION =====

    @FXML
    private void showDashboard() {
        setActivePage(dashboardPane, btnDashboard, "Tableau de bord");
        loadStats();
    }

    @FXML
    private void showUsers() {
        setActivePage(usersPane, btnUsers, "Gestion des Utilisateurs");
        loadPane(usersPane, "UserManagement.fxml");
    }

    @FXML
    private void showDoctors() {
        setActivePage(doctorsPane, btnDoctors, "Gestion des Médecins");
        loadPane(doctorsPane, "DoctorManagement.fxml");
    }

    /** Show one pane and hide others; update sidebar active state */
    private void setActivePage(VBox targetPane, Button activeBtn, String title) {
        // Hide all panes
        dashboardPane.setVisible(false);
        usersPane.setVisible(false);
        doctorsPane.setVisible(false);

        // Show target
        targetPane.setVisible(true);

        // Reset all sidebar buttons
        btnDashboard.getStyleClass().removeAll("sidebar-btn-active");
        btnUsers.getStyleClass().removeAll("sidebar-btn-active");
        btnDoctors.getStyleClass().removeAll("sidebar-btn-active");

        // Set active
        if (!activeBtn.getStyleClass().contains("sidebar-btn-active")) {
            activeBtn.getStyleClass().add("sidebar-btn-active");
        }

        // Update top bar title
        pageTitle.setText(title);
    }

    /** Load an FXML into a VBox container */
    private void loadPane(VBox container, String fxmlName) {
        if (!container.getChildren().isEmpty()) return; // already loaded
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/gestion_medicale/" + fxmlName)
            );
            Node content = loader.load();
            // Make the loaded content grow vertically
            VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().add(content);
        } catch (IOException e) {
            Label err = new Label("Erreur lors du chargement : " + fxmlName);
            err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
            container.getChildren().add(err);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            currentUser = null;
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/gestion_medicale/login.fxml")
            );
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion — Gestion Médicale");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}