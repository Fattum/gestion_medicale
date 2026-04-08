package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.SessionManager;
import com.example.gestion_medicale.models.Ordonnance;
import com.example.gestion_medicale.models.RendezVous;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrdonnanceController {

    @FXML private TableView<Ordonnance> tableOrdonnances;
    @FXML private TableColumn<Ordonnance, LocalDate> colDateOrd;
    @FXML private TableColumn<Ordonnance, String> colLibelle;
    @FXML private TableColumn<Ordonnance, String> colPatient;
    @FXML private TableColumn<Ordonnance, String> colMedecin;

    @FXML private VBox boxCreate;
    @FXML private Label lblSectionTitle;
    @FXML private ComboBox<RendezVous> cmbRdv;
    @FXML private DatePicker dpDateOrdonnance;
    @FXML private TextField txtLibelle;
    @FXML private TextArea txtContenu;
    @FXML private Button btnAjouter;

    @FXML private Label lblLibelle;
    @FXML private TextArea txtContenuView;
    @FXML private Label lblMessage;

    private final ObservableList<Ordonnance> ordonnanceList = FXCollections.observableArrayList();
    private final ObservableList<RendezVous> rdvList = FXCollections.observableArrayList();
    private Ordonnance selected;

    @FXML
    public void initialize() {
        colDateOrd.setCellValueFactory(new PropertyValueFactory<>("dateOrdonnance"));
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecinNom"));

        tableOrdonnances.setItems(ordonnanceList);
        tableOrdonnances.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selected = newVal;
            showDetails(newVal);
        });

        if (cmbRdv != null) {
            cmbRdv.setItems(rdvList);
        }

        configureByRole();
        loadOrdonnances();
        if (SessionManager.getInstance().isMedecin()) {
            loadRdvForMedecin();
        }
    }

    private void configureByRole() {
        var session = SessionManager.getInstance();
        boolean isMedecin = session.isMedecin();
        boolean isPatient = session.isPatient();

        if (boxCreate != null) boxCreate.setManaged(isMedecin);
        if (boxCreate != null) boxCreate.setVisible(isMedecin);
        if (btnAjouter != null) btnAjouter.setDisable(!isMedecin);

        if (lblSectionTitle != null) {
            if (isMedecin) lblSectionTitle.setText("Créer une ordonnance");
            else if (isPatient) lblSectionTitle.setText("Mes ordonnances");
            else lblSectionTitle.setText("Ordonnances");
        }

        if (isMedecin) {
            if (dpDateOrdonnance != null) dpDateOrdonnance.setValue(LocalDate.now());
        }
    }

    private void loadRdvForMedecin() {
        rdvList.clear();
        int medecinId = SessionManager.getInstance().getCurrentUser().getId();
        String sql = """
                SELECT r.id, r.date_rdv, r.heure_rdv, r.statut,
                       r.id_patient, p.nom AS patientNom,
                       r.id_medecin, u.nom AS medecinNom,
                       r.id_secretaire, r.id_disponibilite
                FROM RendezVous r
                JOIN Patient p ON r.id_patient = p.id
                JOIN Utilisateur u ON r.id_medecin = u.id
                WHERE r.id_medecin = ?
                ORDER BY r.date_rdv DESC, r.heure_rdv DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RendezVous rdv = new RendezVous(
                        rs.getInt("id"),
                        rs.getDate("date_rdv").toLocalDate(),
                        rs.getTime("heure_rdv").toLocalTime(),
                        rs.getString("statut"),
                        rs.getInt("id_patient"), rs.getString("patientNom"),
                        rs.getInt("id_medecin"), rs.getString("medecinNom"),
                        rs.getObject("id_secretaire") != null ? rs.getInt("id_secretaire") : null,
                        rs.getObject("id_disponibilite") != null ? rs.getInt("id_disponibilite") : null
                );
                rdvList.add(rdv);
            }
        } catch (SQLException e) {
            showMessage("Erreur chargement RDV: " + e.getMessage(), true);
        }

        if (cmbRdv != null) {
            cmbRdv.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(RendezVous item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null :
                            "#" + item.getId() + " - " + item.getDateRdv() + " " + item.getHeureRdv()
                                    + " - " + item.getPatientNom());
                }
            });
            cmbRdv.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(RendezVous item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null :
                            "#" + item.getId() + " - " + item.getDateRdv() + " " + item.getHeureRdv()
                                    + " - " + item.getPatientNom());
                }
            });
        }
    }

    private void loadOrdonnances() {
        ordonnanceList.clear();
        var session = SessionManager.getInstance();
        boolean isMedecin = session.isMedecin();
        boolean isPatient = session.isPatient();

        String baseSql = """
                SELECT o.id, o.id_rdv, o.id_patient, o.id_medecin,
                       o.libelle, o.contenu, o.date_ordonnance, o.created_at,
                       r.date_rdv, r.heure_rdv,
                       p.nom AS patientNom,
                       u.nom AS medecinNom
                FROM Ordonnance o
                JOIN RendezVous r ON o.id_rdv = r.id
                JOIN Patient p ON o.id_patient = p.id
                JOIN Utilisateur u ON o.id_medecin = u.id
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isPatient) {
                Integer patientId = session.getCurrentPatientId();
                if (patientId == null) {
                    showMessage("Compte patient non lié.", true);
                    return;
                }
                String sql = baseSql + " WHERE o.id_patient = ? ORDER BY o.date_ordonnance DESC, o.id DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, patientId);
                    fillOrdonnances(stmt.executeQuery());
                }
            } else if (isMedecin) {
                String sql = baseSql + " WHERE o.id_medecin = ? ORDER BY o.date_ordonnance DESC, o.id DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, session.getCurrentUser().getId());
                    fillOrdonnances(stmt.executeQuery());
                }
            } else {
                String sql = baseSql + " ORDER BY o.date_ordonnance DESC, o.id DESC";
                try (Statement stmt = conn.createStatement()) {
                    fillOrdonnances(stmt.executeQuery(sql));
                }
            }
        } catch (SQLException e) {
            showMessage("Erreur chargement ordonnances: " + e.getMessage(), true);
        }
    }

    private void fillOrdonnances(ResultSet rs) throws SQLException {
        while (rs.next()) {
            Timestamp ts = rs.getTimestamp("created_at");
            LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;
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
                    createdAt
            ));
        }
    }

    @FXML
    private void handleAjouter() {
        if (!SessionManager.getInstance().isMedecin()) {
            showMessage("Action non autorisée.", true);
            return;
        }
        RendezVous rdv = cmbRdv != null ? cmbRdv.getValue() : null;
        LocalDate dateOrd = dpDateOrdonnance != null ? dpDateOrdonnance.getValue() : null;
        String libelle = txtLibelle != null ? txtLibelle.getText().trim() : "";
        String contenu = txtContenu != null ? txtContenu.getText().trim() : "";

        if (rdv == null || dateOrd == null || libelle.isEmpty()) {
            showMessage("Veuillez sélectionner un RDV, une date et un libellé.", true);
            return;
        }

        String sql = """
                INSERT INTO Ordonnance (id_rdv, id_patient, id_medecin, libelle, contenu, date_ordonnance)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rdv.getId());
            stmt.setInt(2, rdv.getIdPatient());
            stmt.setInt(3, SessionManager.getInstance().getCurrentUser().getId());
            stmt.setString(4, libelle);
            stmt.setString(5, contenu.isEmpty() ? null : contenu);
            stmt.setDate(6, Date.valueOf(dateOrd));
            stmt.executeUpdate();

            showMessage("Ordonnance enregistrée.", false);
            clearCreateForm();
            loadOrdonnances();
        } catch (SQLException e) {
            showMessage("Erreur enregistrement: " + e.getMessage(), true);
        }
    }

    private void clearCreateForm() {
        if (cmbRdv != null) cmbRdv.setValue(null);
        if (dpDateOrdonnance != null) dpDateOrdonnance.setValue(LocalDate.now());
        if (txtLibelle != null) txtLibelle.clear();
        if (txtContenu != null) txtContenu.clear();
    }

    private void showDetails(Ordonnance o) {
        if (lblLibelle != null) lblLibelle.setText(o != null ? o.getLibelle() : "");
        if (txtContenuView != null) txtContenuView.setText(o != null && o.getContenu() != null ? o.getContenu() : "");
    }

    private void showMessage(String msg, boolean error) {
        if (lblMessage == null) return;
        lblMessage.setText(msg);
        lblMessage.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}

