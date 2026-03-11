package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.SessionManager;
import com.example.gestion_medicale.models.Disponibilite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class DisponibiliteController {

    @FXML private TableView<Disponibilite> tableDispos;
    @FXML private TableColumn<Disponibilite, LocalDate> colDate;
    @FXML private TableColumn<Disponibilite, LocalTime> colDebut;
    @FXML private TableColumn<Disponibilite, LocalTime> colFin;
    @FXML private TableColumn<Disponibilite, String> colMedecin;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtHeureDebut;
    @FXML private TextField txtHeureFin;
    @FXML private Label lblMessage;

    private ObservableList<Disponibilite> dispoList = FXCollections.observableArrayList();
    private Disponibilite selectedDispo;
    private int medecinId;

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDispo"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        if (colMedecin != null) {
            colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecinNom"));
        }

        medecinId = SessionManager.getInstance().getCurrentUser().getId();

        tableDispos.setItems(dispoList);
        tableDispos.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDispo = newVal;
                dpDate.setValue(newVal.getDateDispo());
                txtHeureDebut.setText(newVal.getHeureDebut().toString());
                txtHeureFin.setText(newVal.getHeureFin().toString());
            }
        });

        loadDisponibilites();
    }

    public void loadDisponibilites() {
        dispoList.clear();
        String sql;
        if (SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isSecretaire()) {
            sql = """
                    SELECT d.id, d.date_dispo, d.heureDebut, d.heureFin, d.id_medecin, u.nom
                    FROM Disponibilite d
                    JOIN Utilisateur u ON d.id_medecin = u.id
                    ORDER BY d.date_dispo, d.heureDebut
                    """;
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
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
        } else {
            sql = """
                    SELECT d.id, d.date_dispo, d.heureDebut, d.heureFin, d.id_medecin, u.nom
                    FROM Disponibilite d
                    JOIN Utilisateur u ON d.id_medecin = u.id
                    WHERE d.id_medecin = ?
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
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;

        LocalDate date = dpDate.getValue();
        LocalTime debut = LocalTime.parse(txtHeureDebut.getText().trim());
        LocalTime fin = LocalTime.parse(txtHeureFin.getText().trim());

        if (!fin.isAfter(debut)) {
            showMessage("L'heure de fin doit être après l'heure de début.", true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Disponibilite (date_dispo, heureDebut, heureFin, id_medecin) VALUES (?, ?, ?, ?)")) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setTime(2, Time.valueOf(debut));
            stmt.setTime(3, Time.valueOf(fin));
            stmt.setInt(4, medecinId);
            stmt.executeUpdate();
            showMessage("Disponibilité ajoutée.", false);
            handleEffacer();
            loadDisponibilites();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleModifier() {
        if (selectedDispo == null) {
            showMessage("Sélectionnez une disponibilité.", true);
            return;
        }
        if (!validateForm()) return;

        LocalDate date = dpDate.getValue();
        LocalTime debut = LocalTime.parse(txtHeureDebut.getText().trim());
        LocalTime fin = LocalTime.parse(txtHeureFin.getText().trim());

        if (!fin.isAfter(debut)) {
            showMessage("L'heure de fin doit être après l'heure de début.", true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Disponibilite SET date_dispo=?, heureDebut=?, heureFin=? WHERE id=?")) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setTime(2, Time.valueOf(debut));
            stmt.setTime(3, Time.valueOf(fin));
            stmt.setInt(4, selectedDispo.getId());
            stmt.executeUpdate();
            showMessage("Disponibilité modifiée.", false);
            handleEffacer();
            loadDisponibilites();
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedDispo == null) {
            showMessage("Sélectionnez une disponibilité.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette disponibilité ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM Disponibilite WHERE id=?")) {
                    stmt.setInt(1, selectedDispo.getId());
                    stmt.executeUpdate();
                    showMessage("Disponibilité supprimée.", false);
                    handleEffacer();
                    loadDisponibilites();
                } catch (SQLException e) {
                    showMessage("Erreur: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    private void handleEffacer() {
        dpDate.setValue(null);
        txtHeureDebut.clear();
        txtHeureFin.clear();
        selectedDispo = null;
        tableDispos.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private boolean validateForm() {
        if (dpDate.getValue() == null) {
            showMessage("Sélectionnez une date.", true);
            return false;
        }
        try {
            LocalTime.parse(txtHeureDebut.getText().trim());
            LocalTime.parse(txtHeureFin.getText().trim());
        } catch (DateTimeParseException e) {
            showMessage("Format heure invalide (HH:MM).", true);
            return false;
        }
        return true;
    }

    private void showMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}