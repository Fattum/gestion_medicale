package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.Specialite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class SpecialiteController {

    @FXML private TableView<Specialite> tableSpecialites;
    @FXML private TableColumn<Specialite, Integer> colId;
    @FXML private TableColumn<Specialite, String> colNom;
    @FXML private TableColumn<Specialite, String> colDescription;
    @FXML private TextField txtNom;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private ObservableList<Specialite> specialiteList = FXCollections.observableArrayList();
    private Specialite selectedSpecialite;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        tableSpecialites.setItems(specialiteList);
        tableSpecialites.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedSpecialite = newVal;
                txtNom.setText(newVal.getNom());
                txtDescription.setText(newVal.getDescription());
            }
        });

        loadSpecialites();
    }

    public void loadSpecialites() {
        specialiteList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nom, description FROM Specialite ORDER BY nom")) {
            while (rs.next()) {
                specialiteList.add(new Specialite(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleAjouter() {
        String nom = txtNom.getText().trim();
        String desc = txtDescription.getText().trim();

        if (nom.isEmpty()) {
            showMessage("Le nom est obligatoire.", true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Specialite (nom, description) VALUES (?, ?)")) {
            stmt.setString(1, nom);
            stmt.setString(2, desc.isEmpty() ? null : desc);
            stmt.executeUpdate();
            showMessage("Spécialité ajoutée.", false);
            handleEffacer();
            loadSpecialites();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleModifier() {
        if (selectedSpecialite == null) {
            showMessage("Sélectionnez une spécialité.", true);
            return;
        }
        String nom = txtNom.getText().trim();
        String desc = txtDescription.getText().trim();

        if (nom.isEmpty()) {
            showMessage("Le nom est obligatoire.", true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Specialite SET nom=?, description=? WHERE id=?")) {
            stmt.setString(1, nom);
            stmt.setString(2, desc.isEmpty() ? null : desc);
            stmt.setInt(3, selectedSpecialite.getId());
            stmt.executeUpdate();
            showMessage("Spécialité modifiée.", false);
            handleEffacer();
            loadSpecialites();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedSpecialite == null) {
            showMessage("Sélectionnez une spécialité.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la spécialité \"" + selectedSpecialite.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM Specialite WHERE id=?")) {
                    stmt.setInt(1, selectedSpecialite.getId());
                    stmt.executeUpdate();
                    showMessage("Spécialité supprimée.", false);
                    handleEffacer();
                    loadSpecialites();
                } catch (SQLException e) {
                    showMessage("Erreur: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    private void handleEffacer() {
        txtNom.clear();
        txtDescription.clear();
        selectedSpecialite = null;
        tableSpecialites.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}