package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.Doctor;
import com.example.gestion_medicale.models.Specialite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class DoctorController {

    @FXML private TableView<Doctor> tableDoctors;
    @FXML private TableColumn<Doctor, Integer> colId;
    @FXML private TableColumn<Doctor, String> colNom;
    @FXML private TableColumn<Doctor, String> colSpecialite;
    @FXML private TextField txtNom;
    @FXML private PasswordField txtMotDePasse;
    @FXML private ComboBox<Specialite> cmbSpecialite;
    @FXML private Label lblMessage;

    private ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private ObservableList<Specialite> specialiteList = FXCollections.observableArrayList();
    private Doctor selectedDoctor;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSpecialite.setCellValueFactory(new PropertyValueFactory<>("nomSpecialite"));

        cmbSpecialite.setItems(specialiteList);

        tableDoctors.setItems(doctorList);
        tableDoctors.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDoctor = newVal;
                txtNom.setText(newVal.getNom());
                txtMotDePasse.clear();
                // Select specialite in combo
                specialiteList.stream()
                        .filter(s -> s.getId() == newVal.getIdSpecialite())
                        .findFirst()
                        .ifPresent(cmbSpecialite::setValue);
            }
        });

        loadSpecialites();
        loadDoctors();
    }

    private void loadSpecialites() {
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
            showMessage("Erreur chargement spécialités: " + e.getMessage(), true);
        }
    }

    public void loadDoctors() {
        doctorList.clear();
        String sql = """
                SELECT u.id, u.nom, u.motDePasse, m.id_specialite, s.nom AS nomSpec
                FROM Utilisateur u
                JOIN Medecin m ON u.id = m.id_utilisateur
                LEFT JOIN Specialite s ON m.id_specialite = s.id
                ORDER BY u.nom
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Doctor d = new Doctor(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("motDePasse"),
                        rs.getInt("id_specialite"),
                        rs.getString("nomSpec")
                );
                doctorList.add(d);
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleAjouter() {
        String nom = txtNom.getText().trim();
        String mdp = txtMotDePasse.getText().trim();
        Specialite spec = cmbSpecialite.getValue();

        if (nom.isEmpty() || mdp.isEmpty()) {
            showMessage("Nom et mot de passe sont obligatoires.", true);
            return;
        }

        String sqlUser = "INSERT INTO Utilisateur (nom, motDePasse, role) VALUES (?, ?, 'MEDECIN')";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nom);
                stmt.setString(2, mdp);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    String sqlMed = "INSERT INTO Medecin (id_utilisateur, id_specialite) VALUES (?, ?)";
                    try (PreparedStatement mStmt = conn.prepareStatement(sqlMed)) {
                        mStmt.setInt(1, newId);
                        if (spec != null) {
                            mStmt.setInt(2, spec.getId());
                        } else {
                            mStmt.setNull(2, Types.INTEGER);
                        }
                        mStmt.executeUpdate();
                    }
                }
                conn.commit();
                showMessage("Médecin ajouté avec succès.", false);
                handleEffacer();
                loadDoctors();
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
        if (selectedDoctor == null) {
            showMessage("Sélectionnez un médecin.", true);
            return;
        }
        String nom = txtNom.getText().trim();
        Specialite spec = cmbSpecialite.getValue();

        if (nom.isEmpty()) {
            showMessage("Le nom est obligatoire.", true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            String mdp = txtMotDePasse.getText().trim();
            if (!mdp.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Utilisateur SET nom=?, motDePasse=? WHERE id=?")) {
                    stmt.setString(1, nom);
                    stmt.setString(2, mdp);
                    stmt.setInt(3, selectedDoctor.getId());
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Utilisateur SET nom=? WHERE id=?")) {
                    stmt.setString(1, nom);
                    stmt.setInt(2, selectedDoctor.getId());
                    stmt.executeUpdate();
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Medecin SET id_specialite=? WHERE id_utilisateur=?")) {
                if (spec != null) {
                    stmt.setInt(1, spec.getId());
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                stmt.setInt(2, selectedDoctor.getId());
                stmt.executeUpdate();
            }
            conn.commit();
            showMessage("Médecin modifié.", false);
            handleEffacer();
            loadDoctors();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedDoctor == null) {
            showMessage("Sélectionnez un médecin.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le Dr. " + selectedDoctor.getNom() + " ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM Utilisateur WHERE id=?")) {
                    stmt.setInt(1, selectedDoctor.getId());
                    stmt.executeUpdate();
                    showMessage("Médecin supprimé.", false);
                    handleEffacer();
                    loadDoctors();
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
        cmbSpecialite.setValue(null);
        selectedDoctor = null;
        tableDoctors.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}