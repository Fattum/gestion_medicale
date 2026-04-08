package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class UserController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TextField txtNom;
    @FXML private PasswordField txtMotDePasse;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Label lblMessage;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser;
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        cmbRole.setItems(FXCollections.observableArrayList("ADMIN", "SECRETAIRE", "MEDECIN"));

        tableUsers.setItems(userList);
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                txtNom.setText(newVal.getNom());
                cmbRole.setValue(newVal.getRole());
                txtMotDePasse.clear();
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        String sql = "SELECT id, nom, motDePasse, role FROM Utilisateur ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("motDePasse"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur de chargement: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleAjouter() {
        String nom = txtNom.getText().trim();
        String mdp = txtMotDePasse.getText().trim();
        String role = cmbRole.getValue();

        if (nom.isEmpty() || mdp.isEmpty() || role == null) {
            showMessage("Veuillez remplir tous les champs.", true);
            return;
        }

        String sqlUser = "INSERT INTO Utilisateur (nom, motDePasse, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nom);
                stmt.setString(2, mdp);
                stmt.setString(3, role);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    String sqlRole = switch (role) {
                        case "ADMIN" -> "INSERT INTO Admin (id_utilisateur) VALUES (?)";
                        case "SECRETAIRE" -> "INSERT INTO Secretaire (id_utilisateur) VALUES (?)";
                        case "MEDECIN" -> "INSERT INTO Medecin (id_utilisateur) VALUES (?)";
                        default -> null;
                    };
                    if (sqlRole != null) {
                        try (PreparedStatement roleStmt = conn.prepareStatement(sqlRole)) {
                            roleStmt.setInt(1, newId);
                            roleStmt.executeUpdate();
                        }
                    }
                }
                conn.commit();
                showMessage("Utilisateur ajouté avec succès.", false);
                handleEffacer();
                loadUsers();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleModifier() {
        if (selectedUser == null) {
            showMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        String nom = txtNom.getText().trim();
        String role = cmbRole.getValue();

        if (nom.isEmpty() || role == null) {
            showMessage("Veuillez remplir les champs nom et rôle.", true);
            return;
        }

        String sql;
        String mdp = txtMotDePasse.getText().trim();
        if (!mdp.isEmpty()) {
            sql = "UPDATE Utilisateur SET nom=?, motDePasse=?, role=? WHERE id=?";
        } else {
            sql = "UPDATE Utilisateur SET nom=?, role=? WHERE id=?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (!mdp.isEmpty()) {
                stmt.setString(1, nom);
                stmt.setString(2, mdp);
                stmt.setString(3, role);
                stmt.setInt(4, selectedUser.getId());
            } else {
                stmt.setString(1, nom);
                stmt.setString(2, role);
                stmt.setInt(3, selectedUser.getId());
            }
            stmt.executeUpdate();
            showMessage("Utilisateur modifié avec succès.", false);
            handleEffacer();
            loadUsers();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedUser == null) {
            showMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'utilisateur \"" + selectedUser.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM Utilisateur WHERE id=?")) {
                    stmt.setInt(1, selectedUser.getId());
                    stmt.executeUpdate();
                    showMessage("Utilisateur supprimé.", false);
                    handleEffacer();
                    loadUsers();
                } catch (SQLException e) {
                    showMessage("Erreur: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    private void handleEffacer() {
        txtNom.clear();
        txtMotDePasse.clear();
        cmbRole.setValue(null);
        selectedUser = null;
        tableUsers.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}