package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class PatientController {

    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colNom;
    @FXML private TableColumn<Patient, String> colTelephone;
    @FXML private TableColumn<Patient, String> colAdresse;
    @FXML private TextField txtNom;
    @FXML private TextField txtTelephone;
    @FXML private TextArea txtAdresse;
    @FXML private Label lblMessage;

    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private Patient selectedPatient;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        tablePatients.setItems(patientList);
        tablePatients.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal;
                txtNom.setText(newVal.getNom());
                txtTelephone.setText(newVal.getTelephone() != null ? newVal.getTelephone() : "");
                txtAdresse.setText(newVal.getAdresse() != null ? newVal.getAdresse() : "");
            }
        });

        loadPatients();
    }

    public void loadPatients() {
        patientList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, nom, telephone, adresse FROM Patient ORDER BY nom")) {
            while (rs.next()) {
                patientList.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("telephone"),
                        rs.getString("adresse")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur de chargement: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleAjouter() {
        String nom = txtNom.getText().trim();
        String tel = txtTelephone.getText().trim();
        String adr = txtAdresse.getText().trim();

        if (nom.isEmpty()) {
            showMessage("Le nom du patient est obligatoire.", true);
            return;
        }

        String sqlPatient = "INSERT INTO Patient (nom, telephone, adresse) VALUES (?, ?, ?)";
        String sqlDossier = "INSERT INTO DossierMedical (id_patient) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sqlPatient, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nom);
                stmt.setString(2, tel.isEmpty() ? null : tel);
                stmt.setString(3, adr.isEmpty() ? null : adr);
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    // Automatically create DossierMedical
                    try (PreparedStatement dStmt = conn.prepareStatement(sqlDossier)) {
                        dStmt.setInt(1, newId);
                        dStmt.executeUpdate();
                    }
                }
                conn.commit();
                showMessage("Patient ajouté et dossier médical créé automatiquement.", false);
                handleEffacer();
                loadPatients();
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
        if (selectedPatient == null) {
            showMessage("Sélectionnez un patient.", true);
            return;
        }
        String nom = txtNom.getText().trim();
        String tel = txtTelephone.getText().trim();
        String adr = txtAdresse.getText().trim();

        if (nom.isEmpty()) {
            showMessage("Le nom est obligatoire.", true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Patient SET nom=?, telephone=?, adresse=? WHERE id=?")) {
            stmt.setString(1, nom);
            stmt.setString(2, tel.isEmpty() ? null : tel);
            stmt.setString(3, adr.isEmpty() ? null : adr);
            stmt.setInt(4, selectedPatient.getId());
            stmt.executeUpdate();
            showMessage("Patient modifié.", false);
            handleEffacer();
            loadPatients();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedPatient == null) {
            showMessage("Sélectionnez un patient.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le patient \"" + selectedPatient.getNom() + "\" et son dossier médical ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM Patient WHERE id=?")) {
                    stmt.setInt(1, selectedPatient.getId());
                    stmt.executeUpdate();
                    showMessage("Patient supprimé.", false);
                    handleEffacer();
                    loadPatients();
                } catch (SQLException e) {
                    showMessage("Erreur: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    private void handleEffacer() {
        txtNom.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        selectedPatient = null;
        tablePatients.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}