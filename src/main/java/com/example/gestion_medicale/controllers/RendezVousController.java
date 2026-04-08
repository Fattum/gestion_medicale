package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.AppExecutors;
import com.example.gestion_medicale.SessionManager;
import com.example.gestion_medicale.models.Disponibilite;
import com.example.gestion_medicale.models.Doctor;
import com.example.gestion_medicale.models.Patient;
import com.example.gestion_medicale.models.RendezVous;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class RendezVousController {

    @FXML private TableView<RendezVous> tableRdv;
    @FXML private TableColumn<RendezVous, LocalDate> colDate;
    @FXML private TableColumn<RendezVous, LocalTime> colHeure;
    @FXML private TableColumn<RendezVous, String> colPatient;
    @FXML private TableColumn<RendezVous, String> colMedecin;
    @FXML private TableColumn<RendezVous, String> colStatut;

    @FXML private ComboBox<Patient> cmbPatient;
    @FXML private ComboBox<Doctor> cmbMedecin;
    @FXML private ComboBox<Disponibilite> cmbDisponibilite;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtHeure;
    @FXML private Label lblMessage;

    private ObservableList<RendezVous> rdvList = FXCollections.observableArrayList();
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private ObservableList<Disponibilite> dispoList = FXCollections.observableArrayList();
    private RendezVous selectedRdv;

    // Map (HashMap) pour accès O(1) au lieu de parcourir les listes
    private final Map<Integer, Patient> patientById = new HashMap<>();
    private final Map<Integer, Doctor> doctorById = new HashMap<>();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureRdv"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecinNom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        cmbStatut.setItems(FXCollections.observableArrayList(
                "PLANIFIE", "CONFIRME", "ANNULE", "TERMINE"));

        cmbPatient.setItems(patientList);
        cmbMedecin.setItems(doctorList);
        cmbDisponibilite.setItems(dispoList);

        // When doctor changes, refresh disponibilites
        cmbMedecin.valueProperty().addListener((obs, old, newDoc) -> {
            if (newDoc != null) loadDisponibilitesByMedecin(newDoc.getId());
        });

        // When disponibilite selected, auto-fill date/heure
        cmbDisponibilite.valueProperty().addListener((obs, old, dispo) -> {
            if (dispo != null) {
                dpDate.setValue(dispo.getDateDispo());
                txtHeure.setText(dispo.getHeureDebut().toString());
            }
        });

        tableRdv.setItems(rdvList);
        tableRdv.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedRdv = newVal;
                dpDate.setValue(newVal.getDateRdv());
                txtHeure.setText(newVal.getHeureRdv().toString());
                cmbStatut.setValue(newVal.getStatut());
                Patient p = patientById.get(newVal.getIdPatient());
                if (p != null) cmbPatient.setValue(p);
                Doctor d = doctorById.get(newVal.getIdMedecin());
                if (d != null) cmbMedecin.setValue(d);
            }
        });

        loadPatientsAsync();
        loadDoctorsAsync();
        loadRendezVousAsync();
    }

    private void loadPatientsAsync() {
        Task<ObservableList<Patient>> task = new Task<>() {
            @Override
            protected ObservableList<Patient> call() throws Exception {
                ObservableList<Patient> results = FXCollections.observableArrayList();
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id, nom, telephone, adresse FROM Patient ORDER BY nom")) {
                    while (rs.next()) {
                        results.add(new Patient(rs.getInt("id"), rs.getString("nom"),
                                rs.getString("telephone"), rs.getString("adresse")));
                    }
                }
                return results;
            }
        };
        task.setOnSucceeded(evt -> {
            patientList.setAll(task.getValue());
            patientById.clear();
            for (Patient p : patientList) patientById.put(p.getId(), p);
        });
        task.setOnFailed(evt -> showMessage("Erreur chargement patients: " + task.getException().getMessage(), true));
        AppExecutors.db().submit(task);
    }

    private void loadDoctorsAsync() {
        Task<ObservableList<Doctor>> task = new Task<>() {
            @Override
            protected ObservableList<Doctor> call() throws Exception {
                ObservableList<Doctor> results = FXCollections.observableArrayList();
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
                        results.add(new Doctor(rs.getInt("id"), rs.getString("nom"),
                                rs.getString("motDePasse"), rs.getInt("id_specialite"), rs.getString("nomSpec")));
                    }
                }
                return results;
            }
        };
        task.setOnSucceeded(evt -> {
            doctorList.setAll(task.getValue());
            doctorById.clear();
            for (Doctor d : doctorList) doctorById.put(d.getId(), d);
        });
        task.setOnFailed(evt -> showMessage("Erreur chargement médecins: " + task.getException().getMessage(), true));
        AppExecutors.db().submit(task);
    }

    private void loadDisponibilitesByMedecin(int medecinId) {
        dispoList.clear();
        String sql = """
                SELECT d.id, d.date_dispo, d.heureDebut, d.heureFin, d.id_medecin, u.nom
                FROM Disponibilite d
                JOIN Utilisateur u ON d.id_medecin = u.id
                WHERE d.id_medecin = ?
                AND d.id NOT IN (SELECT id_disponibilite FROM RendezVous WHERE id_disponibilite IS NOT NULL)
                ORDER BY d.date_dispo, d.heureDebut
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dispoList.add(new Disponibilite(
                        rs.getInt("id"),
                        rs.getDate("date_dispo").toLocalDate(),
                        rs.getTime("heureDebut").toLocalTime(),
                        rs.getTime("heureFin").toLocalTime(),
                        rs.getInt("id_medecin"),
                        rs.getString("nom")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    public void loadRendezVousAsync() {
        String sql;
        boolean isMedecin = SessionManager.getInstance().isMedecin();

        Task<ObservableList<RendezVous>> task = new Task<>() {
            @Override
            protected ObservableList<RendezVous> call() throws Exception {
                ObservableList<RendezVous> results = FXCollections.observableArrayList();
                if (isMedecin) {
                    String q = """
                            SELECT r.id, r.date_rdv, r.heure_rdv, r.statut,
                                   r.id_patient, p.nom AS patientNom,
                                   r.id_medecin, u.nom AS medecinNom,
                                   r.id_secretaire, r.id_disponibilite
                            FROM RendezVous r
                            JOIN Patient p ON r.id_patient = p.id
                            JOIN Utilisateur u ON r.id_medecin = u.id
                            WHERE r.id_medecin = ?
                            ORDER BY r.date_rdv DESC, r.heure_rdv
                            """;
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(q)) {
                        stmt.setInt(1, SessionManager.getInstance().getCurrentUser().getId());
                        try (ResultSet rs = stmt.executeQuery()) {
                            fillRdvFromResultSet(rs, results);
                        }
                    }
                } else {
                    String q = """
                            SELECT r.id, r.date_rdv, r.heure_rdv, r.statut,
                                   r.id_patient, p.nom AS patientNom,
                                   r.id_medecin, u.nom AS medecinNom,
                                   r.id_secretaire, r.id_disponibilite
                            FROM RendezVous r
                            JOIN Patient p ON r.id_patient = p.id
                            JOIN Utilisateur u ON r.id_medecin = u.id
                            ORDER BY r.date_rdv DESC, r.heure_rdv
                            """;
                    try (Connection conn = DatabaseConnection.getConnection();
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(q)) {
                        fillRdvFromResultSet(rs, results);
                    }
                }
                return results;
            }
        };
        task.setOnSucceeded(evt -> rdvList.setAll(task.getValue()));
        task.setOnFailed(evt -> showMessage("Erreur: " + task.getException().getMessage(), true));
        AppExecutors.db().submit(task);
    }

    private void fillRdvFromResultSet(ResultSet rs, ObservableList<RendezVous> target) throws SQLException {
        while (rs.next()) {
            target.add(new RendezVous(
                    rs.getInt("id"),
                    rs.getDate("date_rdv").toLocalDate(),
                    rs.getTime("heure_rdv").toLocalTime(),
                    rs.getString("statut"),
                    rs.getInt("id_patient"), rs.getString("patientNom"),
                    rs.getInt("id_medecin"), rs.getString("medecinNom"),
                    rs.getObject("id_secretaire") != null ? rs.getInt("id_secretaire") : null,
                    rs.getObject("id_disponibilite") != null ? rs.getInt("id_disponibilite") : null
            ));
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;

        Patient patient = cmbPatient.getValue();
        Doctor medecin = cmbMedecin.getValue();
        Disponibilite dispo = cmbDisponibilite.getValue();
        String statut = cmbStatut.getValue() != null ? cmbStatut.getValue() : "PLANIFIE";
        LocalDate date = dpDate.getValue();
        LocalTime heure;
        try {
            heure = LocalTime.parse(txtHeure.getText().trim());
        } catch (DateTimeParseException e) {
            showMessage("Format heure invalide (HH:MM).", true);
            return;
        }

        Integer secretaireId = SessionManager.getInstance().isSecretaire()
                ? SessionManager.getInstance().getCurrentUser().getId() : null;

        String sql = """
                INSERT INTO RendezVous (date_rdv, heure_rdv, statut, id_patient, id_medecin,
                                        id_secretaire, id_disponibilite)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setTime(2, Time.valueOf(heure));
            stmt.setString(3, statut);
            stmt.setInt(4, patient.getId());
            stmt.setInt(5, medecin.getId());
            if (secretaireId != null) stmt.setInt(6, secretaireId);
            else stmt.setNull(6, Types.INTEGER);
            if (dispo != null) stmt.setInt(7, dispo.getId());
            else stmt.setNull(7, Types.INTEGER);
            stmt.executeUpdate();
            showMessage("Rendez-vous ajouté.", false);
            handleEffacer();
            loadRendezVousAsync();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleModifierStatut() {
        if (selectedRdv == null) {
            showMessage("Sélectionnez un rendez-vous.", true);
            return;
        }
        String statut = cmbStatut.getValue();
        if (statut == null) {
            showMessage("Sélectionnez un statut.", true);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE RendezVous SET statut=? WHERE id=?")) {
            stmt.setString(1, statut);
            stmt.setInt(2, selectedRdv.getId());
            stmt.executeUpdate();
            showMessage("Statut mis à jour.", false);
            handleEffacer();
            loadRendezVousAsync();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedRdv == null) {
            showMessage("Sélectionnez un rendez-vous.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM RendezVous WHERE id=?")) {
                    stmt.setInt(1, selectedRdv.getId());
                    stmt.executeUpdate();
                    showMessage("Rendez-vous supprimé.", false);
                    handleEffacer();
                    loadRendezVousAsync();
                } catch (SQLException e) {
                    showMessage("Erreur: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    private void handleEffacer() {
        dpDate.setValue(null);
        txtHeure.clear();
        cmbPatient.setValue(null);
        cmbMedecin.setValue(null);
        cmbDisponibilite.setValue(null);
        cmbStatut.setValue(null);
        selectedRdv = null;
        tableRdv.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private boolean validateForm() {
        if (cmbPatient.getValue() == null) {
            showMessage("Sélectionnez un patient.", true);
            return false;
        }
        if (cmbMedecin.getValue() == null) {
            showMessage("Sélectionnez un médecin.", true);
            return false;
        }
        if (dpDate.getValue() == null) {
            showMessage("Sélectionnez une date.", true);
            return false;
        }
        return true;
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}