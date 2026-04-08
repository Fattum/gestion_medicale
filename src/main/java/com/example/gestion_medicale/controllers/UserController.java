package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.Doctor;
import com.example.gestion_medicale.models.Patient;
import com.example.gestion_medicale.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TextField txtNom;
    @FXML private PasswordField txtMotDePasse;
    @FXML private ComboBox<String> cmbRole;
    @FXML private VBox boxPatientLink;
    @FXML private ComboBox<Patient> cmbPatientLink;
    @FXML private VBox boxSecretaireDoctors;
    @FXML private CheckComboBox<Doctor> cmbDoctorsLink;
    @FXML private VBox boxDoctorInfo;
    @FXML private ComboBox<Doctor> cmbMedecinLink;
    @FXML private Label lblMessage;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private User selectedUser;
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        cmbRole.setItems(FXCollections.observableArrayList("ADMIN", "SECRETAIRE", "MEDECIN", "PATIENT"));

        if (cmbPatientLink != null) {
            cmbPatientLink.setItems(patientList);
            loadPatients();
        }

        if (boxPatientLink != null) {
            boxPatientLink.setManaged(false);
            boxPatientLink.setVisible(false);
        }

        if (cmbDoctorsLink != null) {
            cmbDoctorsLink.getItems().setAll(doctorList);
            loadDoctors();
        }

        if (boxSecretaireDoctors != null) {
            boxSecretaireDoctors.setManaged(false);
            boxSecretaireDoctors.setVisible(false);
        }

        if (boxDoctorInfo != null) {
            boxDoctorInfo.setManaged(false);
            boxDoctorInfo.setVisible(false);
        }

        if (cmbMedecinLink != null) {
            cmbMedecinLink.setItems(doctorList);
        }

        if (cmbRole != null) {
            cmbRole.valueProperty().addListener((obs, old, role) -> {
                togglePatientLink(role);
                toggleSecretaireDoctors(role);
                toggleDoctorInfo(role);
            });
        }

        tableUsers.setItems(userList);
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                txtNom.setText(newVal.getNom());
                cmbRole.setValue(newVal.getRole());
                txtMotDePasse.clear();
                if (cmbPatientLink != null) cmbPatientLink.setValue(null);
                if (cmbDoctorsLink != null) cmbDoctorsLink.getCheckModel().clearChecks();
                if (cmbMedecinLink != null) cmbMedecinLink.setValue(null);
            }
        });

        loadUsers();
    }

    private void togglePatientLink(String role) {
        boolean patient = "PATIENT".equals(role);
        if (boxPatientLink != null) {
            boxPatientLink.setManaged(patient);
            boxPatientLink.setVisible(patient);
        }
        if (!patient && cmbPatientLink != null) {
            cmbPatientLink.setValue(null);
        }
    }

    private void toggleSecretaireDoctors(String role) {
        boolean sec = "SECRETAIRE".equals(role);
        if (boxSecretaireDoctors != null) {
            boxSecretaireDoctors.setManaged(sec);
            boxSecretaireDoctors.setVisible(sec);
        }
        if (!sec && cmbDoctorsLink != null) {
            cmbDoctorsLink.getCheckModel().clearChecks();
        }
    }

    private void toggleDoctorInfo(String role) {
        boolean med = "MEDECIN".equals(role);
        if (boxDoctorInfo != null) {
            boxDoctorInfo.setManaged(med);
            boxDoctorInfo.setVisible(med);
        }
        if (!med && cmbMedecinLink != null) {
            cmbMedecinLink.setValue(null);
        }
    }

    private void loadPatients() {
        patientList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nom, telephone, adresse FROM Patient ORDER BY nom")) {
            while (rs.next()) {
                patientList.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("telephone"),
                        rs.getString("adresse")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur chargement patients: " + e.getMessage(), true);
        }
    }

    private void loadDoctors() {
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
                doctorList.add(new Doctor(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("motDePasse"),
                        rs.getInt("id_specialite"),
                        rs.getString("nomSpec")
                ));
            }
            if (cmbDoctorsLink != null) {
                cmbDoctorsLink.getItems().setAll(doctorList);
            }
            if (cmbMedecinLink != null) {
                cmbMedecinLink.setItems(doctorList);
            }
        } catch (SQLException e) {
            showMessage("Erreur chargement médecins: " + e.getMessage(), true);
        }
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

        Patient linkedPatient = null;
        if ("PATIENT".equals(role)) {
            linkedPatient = cmbPatientLink != null ? cmbPatientLink.getValue() : null;
            if (linkedPatient == null) {
                showMessage("Sélectionnez le patient à lier au compte.", true);
                return;
            }
        }

        List<Doctor> linkedDoctors = new ArrayList<>();
        if ("SECRETAIRE".equals(role)) {
            if (cmbDoctorsLink != null) {
                linkedDoctors.addAll(cmbDoctorsLink.getCheckModel().getCheckedItems());
            }
            if (linkedDoctors.isEmpty()) {
                showMessage("Sélectionnez au moins un médecin à lier au secrétaire.", true);
                return;
            }
        }

        if ("MEDECIN".equals(role)) {
            Doctor selectedMed = cmbMedecinLink != null ? cmbMedecinLink.getValue() : null;
            if (selectedMed == null) {
                showMessage("Sélectionnez le médecin à lier au compte.", true);
                return;
            }
            // Lier un compte à un médecin EXISTANT: on met à jour son mot de passe (et éventuellement le nom)
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE Utilisateur SET nom=?, motDePasse=?, role='MEDECIN' WHERE id=?")) {
                stmt.setString(1, nom);
                stmt.setString(2, mdp);
                stmt.setInt(3, selectedMed.getId());
                int updated = stmt.executeUpdate();
                if (updated == 1) {
                    showMessage("Compte médecin mis à jour (lié au médecin sélectionné).", false);
                    handleEffacer();
                    loadUsers();
                } else {
                    showMessage("Impossible de lier ce médecin.", true);
                }
            } catch (SQLException e) {
                showMessage("Erreur: " + e.getMessage(), true);
            }
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
                        case "PATIENT" -> "INSERT INTO PatientCompte (id_utilisateur, id_patient) VALUES (?, ?)";
                        default -> null;
                    };
                    if (sqlRole != null) {
                        try (PreparedStatement roleStmt = conn.prepareStatement(sqlRole)) {
                            if ("PATIENT".equals(role)) {
                                roleStmt.setInt(1, newId);
                                roleStmt.setInt(2, linkedPatient.getId());
                                roleStmt.executeUpdate();
                            } else {
                                roleStmt.setInt(1, newId);
                                roleStmt.executeUpdate();
                            }
                        }
                    }

                    if ("SECRETAIRE".equals(role)) {
                        String linkSql = "INSERT INTO SecretaireMedecin (id_secretaire, id_medecin) VALUES (?, ?)";
                        try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                            for (Doctor d : linkedDoctors) {
                                linkStmt.setInt(1, newId);
                                linkStmt.setInt(2, d.getId());
                                linkStmt.addBatch();
                            }
                            linkStmt.executeBatch();
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
        if (cmbPatientLink != null) cmbPatientLink.setValue(null);
        if (cmbMedecinLink != null) cmbMedecinLink.setValue(null);
        togglePatientLink(null);
        toggleSecretaireDoctors(null);
        toggleDoctorInfo(null);
        selectedUser = null;
        tableUsers.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}