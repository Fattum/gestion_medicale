package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.SessionManager;
import com.example.gestion_medicale.models.DossierMedical;
import com.example.gestion_medicale.models.Ordonnance;
import com.example.gestion_medicale.models.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;

public class DossierMedicalController {

    @FXML private TableView<DossierMedical> tableDossiers;
    @FXML private TableColumn<DossierMedical, Integer> colId;
    @FXML private TableColumn<DossierMedical, String> colPatient;
    @FXML private ComboBox<Patient> cmbPatient;
    @FXML private TextArea txtHistorique;
    @FXML private TextArea txtAllergies;
    @FXML private TextArea txtObservations;
    @FXML private TableView<Ordonnance> tableOrdonnances;
    @FXML private TableColumn<Ordonnance, LocalDate> colOrdDate;
    @FXML private TableColumn<Ordonnance, String> colOrdLibelle;
    @FXML private Label lblMessage;
    @FXML private Button btnAjouter;

    private ObservableList<DossierMedical> dossierList = FXCollections.observableArrayList();
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private ObservableList<Ordonnance> ordonnanceList = FXCollections.observableArrayList();
    private DossierMedical selectedDossier;
    private boolean isMedecin;
    private boolean isPatient;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));

        isMedecin = SessionManager.getInstance().isMedecin();
        isPatient = SessionManager.getInstance().isPatient();

        if (tableOrdonnances != null) {
            colOrdDate.setCellValueFactory(new PropertyValueFactory<>("dateOrdonnance"));
            colOrdLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
            tableOrdonnances.setItems(ordonnanceList);
        }

        // Medecins can only modify, not add new dossiers (auto-created with patient)
        if (btnAjouter != null) {
            btnAjouter.setVisible(!isMedecin && !isPatient);
        }

        if (cmbPatient != null) {
            cmbPatient.setItems(patientList);
            if (!isPatient) {
                loadPatients();
            } else {
                cmbPatient.setDisable(true);
                cmbPatient.setVisible(false);
                cmbPatient.setManaged(false);
            }
        }

        tableDossiers.setItems(dossierList);
        tableDossiers.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDossier = newVal;
                if (txtHistorique != null) txtHistorique.setText(newVal.getHistorique() != null ? newVal.getHistorique() : "");
                if (txtAllergies != null) txtAllergies.setText(newVal.getAllergies() != null ? newVal.getAllergies() : "");
                if (txtObservations != null) txtObservations.setText(newVal.getObservations() != null ? newVal.getObservations() : "");
                if (cmbPatient != null) {
                    patientList.stream()
                            .filter(p -> p.getId() == newVal.getIdPatient())
                            .findFirst()
                            .ifPresent(cmbPatient::setValue);
                }
                loadOrdonnancesForPatient(newVal.getIdPatient());
            }
        });

        if (isPatient) {
            loadDossierForCurrentPatient();
        } else if (isMedecin) {
            loadDossiersByMedecin();
        } else {
            loadAllDossiers();
        }
    }

    private void loadPatients() {
        patientList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nom, telephone, adresse FROM Patient ORDER BY nom")) {
            while (rs.next()) {
                patientList.add(new Patient(rs.getInt("id"), rs.getString("nom"),
                        rs.getString("telephone"), rs.getString("adresse")));
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    public void loadAllDossiers() {
        dossierList.clear();
        String sql = """
                SELECT d.id, d.id_patient, p.nom AS patientNom, d.historique, d.allergies, d.observations
                FROM DossierMedical d
                JOIN Patient p ON d.id_patient = p.id
                ORDER BY p.nom
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dossierList.add(new DossierMedical(
                        rs.getInt("id"), rs.getInt("id_patient"), rs.getString("patientNom"),
                        rs.getString("historique"), rs.getString("allergies"), rs.getString("observations")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    public void loadDossiersByMedecin() {
        dossierList.clear();
        int medecinId = SessionManager.getInstance().getCurrentUser().getId();
        String sql = """
                SELECT DISTINCT d.id, d.id_patient, p.nom AS patientNom, d.historique, d.allergies, d.observations
                FROM DossierMedical d
                JOIN Patient p ON d.id_patient = p.id
                JOIN RendezVous r ON r.id_patient = p.id
                WHERE r.id_medecin = ?
                ORDER BY p.nom
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dossierList.add(new DossierMedical(
                        rs.getInt("id"), rs.getInt("id_patient"), rs.getString("patientNom"),
                        rs.getString("historique"), rs.getString("allergies"), rs.getString("observations")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    public void loadDossierForCurrentPatient() {
        dossierList.clear();
        Integer patientId = SessionManager.getInstance().getCurrentPatientId();
        if (patientId == null) {
            showMessage("Compte patient non lié.", true);
            return;
        }
        String sql = """
                SELECT d.id, d.id_patient, p.nom AS patientNom, d.historique, d.allergies, d.observations
                FROM DossierMedical d
                JOIN Patient p ON d.id_patient = p.id
                WHERE d.id_patient = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dossierList.add(new DossierMedical(
                        rs.getInt("id"), rs.getInt("id_patient"), rs.getString("patientNom"),
                        rs.getString("historique"), rs.getString("allergies"), rs.getString("observations")
                ));
            }
            if (!dossierList.isEmpty()) {
                tableDossiers.getSelectionModel().select(0);
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    private void loadOrdonnancesForPatient(int patientId) {
        ordonnanceList.clear();
        String sql = """
                SELECT o.id, o.id_rdv, o.id_patient, o.id_medecin,
                       o.libelle, o.contenu, o.date_ordonnance, o.created_at,
                       r.date_rdv, r.heure_rdv,
                       p.nom AS patientNom,
                       u.nom AS medecinNom
                FROM Ordonnance o
                JOIN RendezVous r ON o.id_rdv = r.id
                JOIN Patient p ON o.id_patient = p.id
                JOIN Utilisateur u ON o.id_medecin = u.id
                WHERE o.id_patient = ?
                ORDER BY o.date_ordonnance DESC, o.id DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                ordonnanceList.add(new Ordonnance(
                        rs.getInt("id"),
                        rs.getInt("id_rdv"),
                        rs.getDate("date_rdv").toLocalDate(),
                        rs.getTime("heure_rdv").toLocalTime().toString(),
                        rs.getInt("id_patient"),
                        rs.getString("patientNom"),
                        rs.getInt("id_medecin"),
                        rs.getString("medecinNom"),
                        rs.getString("libelle"),
                        rs.getString("contenu"),
                        rs.getDate("date_ordonnance").toLocalDate(),
                        ts != null ? ts.toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur chargement ordonnances: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleModifier() {
        if (selectedDossier == null) {
            showMessage("Sélectionnez un dossier.", true);
            return;
        }
        String hist = txtHistorique != null ? txtHistorique.getText().trim() : "";
        String allerg = txtAllergies != null ? txtAllergies.getText().trim() : "";
        String obs = txtObservations != null ? txtObservations.getText().trim() : "";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE DossierMedical SET historique=?, allergies=?, observations=? WHERE id=?")) {
            stmt.setString(1, hist.isEmpty() ? null : hist);
            stmt.setString(2, allerg.isEmpty() ? null : allerg);
            stmt.setString(3, obs.isEmpty() ? null : obs);
            stmt.setInt(4, selectedDossier.getId());
            stmt.executeUpdate();
            showMessage("Dossier médical mis à jour.", false);
            handleEffacer();
            if (isMedecin) loadDossiersByMedecin();
            else loadAllDossiers();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleEffacer() {
        if (txtHistorique != null) txtHistorique.clear();
        if (txtAllergies != null) txtAllergies.clear();
        if (txtObservations != null) txtObservations.clear();
        if (cmbPatient != null) cmbPatient.setValue(null);
        ordonnanceList.clear();
        selectedDossier = null;
        tableDossiers.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}