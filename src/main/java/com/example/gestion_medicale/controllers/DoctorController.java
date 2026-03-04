package com.example.gestion_medicale.controllers;
import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.Doctor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Contrôleur CRUD pour les médecins - Design moderne
 */
public class DoctorController implements Initializable {

    // Table
    @FXML private TableView<Doctor> doctorTable;
    @FXML private TableColumn<Doctor, Integer> colId;
    @FXML private TableColumn<Doctor, String>  colNom;
    @FXML private TableColumn<Doctor, String>  colPrenom;
    @FXML private TableColumn<Doctor, String>  colSpecialite;
    @FXML private TableColumn<Doctor, Integer> colUserId;
    @FXML private TableColumn<Doctor, Void>    colActions;

    // Search & count
    @FXML private TextField searchField;
    @FXML private Label     countLabel;

    // Form fields
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField specialiteField;
    @FXML private ComboBox<String> userIdCombo;
    @FXML private TextField idField;

    // Form UI
    @FXML private Label   formTitle;
    @FXML private Label   messageLabel;
    @FXML private Button  saveBtn;
    @FXML private Button  deleteBtn;

    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private boolean isEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadUsersIntoCombo();
        loadDoctors();
        clearForm();
    }

    // ===== TABLE SETUP =====

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colSpecialite.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));

        // Specialite column with teal badge
        colSpecialite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String spec, boolean empty) {
                super.updateItem(spec, empty);
                if (empty || spec == null) {
                    setGraphic(null); setText(null);
                } else {
                    Label badge = new Label(spec);
                    badge.setStyle(
                        "-fx-background-color: rgba(6,182,212,0.12);" +
                        "-fx-text-fill: #0891b2;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 3 10 3 10;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
                    );
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Modifier");
            private final Button delBtn  = new Button("🗑 Supprimer");
            private final HBox   box     = new HBox(6, editBtn, delBtn);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                editBtn.setStyle(
                    "-fx-background-color: rgba(59,130,246,0.1);" +
                    "-fx-text-fill: #3b82f6;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 4 10 4 10;" +
                    "-fx-font-size: 11px;" +
                    "-fx-cursor: hand;"
                );
                delBtn.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.1);" +
                    "-fx-text-fill: #ef4444;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 4 10 4 10;" +
                    "-fx-font-size: 11px;" +
                    "-fx-cursor: hand;"
                );
                editBtn.setOnAction(e -> {
                    Doctor d = getTableView().getItems().get(getIndex());
                    populateForm(d);
                });
                delBtn.setOnAction(e -> {
                    Doctor d = getTableView().getItems().get(getIndex());
                    confirmDelete(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        doctorTable.setItems(doctorList);
    }

    // ===== LOAD DATA =====

    private void loadUsersIntoCombo() {
        ObservableList<String> userOptions = FXCollections.observableArrayList();
        String sql = "SELECT id, username FROM users ORDER BY username";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                userOptions.add(rs.getInt("id") + " — " + rs.getString("username"));
            }
        } catch (SQLException e) {
            // silent
        }
        userIdCombo.setItems(userOptions);
    }

    private void loadDoctors() {
        doctorList.clear();
        String sql = "SELECT id, nom, prenom, specialite, user_id FROM doctors ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctorList.add(new Doctor(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("specialite"),
                    rs.getInt("user_id")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur de chargement : " + e.getMessage(), false);
        }
        updateCount();
    }

    private void updateCount() {
        countLabel.setText(doctorList.size() + " médecin(s)");
    }

    // ===== SEARCH =====

    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            doctorTable.setItems(doctorList);
            countLabel.setText(doctorList.size() + " médecin(s)");
            return;
        }
        ObservableList<Doctor> filtered = FXCollections.observableArrayList();
        for (Doctor d : doctorList) {
            if (d.getNom().toLowerCase().contains(q) ||
                d.getPrenom().toLowerCase().contains(q) ||
                d.getSpecialite().toLowerCase().contains(q)) {
                filtered.add(d);
            }
        }
        doctorTable.setItems(filtered);
        countLabel.setText(filtered.size() + " résultat(s)");
    }

    // ===== FORM ACTIONS =====

    @FXML
    private void showAddForm() {
        clearForm();
        formTitle.setText("➕ Ajouter un médecin");
        saveBtn.setText("💾  Enregistrer");
        deleteBtn.setVisible(false);
        deleteBtn.setManaged(false);
    }

    @FXML
    private void onTableRowSelected() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected != null) populateForm(selected);
    }

    private void populateForm(Doctor d) {
        isEditMode = true;
        formTitle.setText("✏ Modifier le médecin");
        idField.setText(String.valueOf(d.getId()));
        nomField.setText(d.getNom());
        prenomField.setText(d.getPrenom());
        specialiteField.setText(d.getSpecialite());
        // Select matching user in combo
        for (String item : userIdCombo.getItems()) {
            if (item.startsWith(d.getUserId() + " — ")) {
                userIdCombo.setValue(item);
                break;
            }
        }
        saveBtn.setText("💾  Mettre à jour");
        deleteBtn.setVisible(true);
        deleteBtn.setManaged(true);
        hideMessage();
    }

    @FXML
    private void handleSave() {
        String nom        = nomField.getText().trim();
        String prenom     = prenomField.getText().trim();
        String specialite = specialiteField.getText().trim();
        String userSel    = userIdCombo.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || specialite.isEmpty() || userSel == null) {
            showMessage("⚠ Veuillez remplir tous les champs.", false);
            return;
        }

        int userId = Integer.parseInt(userSel.split(" — ")[0]);

        if (isEditMode) {
            updateDoctor(Integer.parseInt(idField.getText()), nom, prenom, specialite, userId);
        } else {
            addDoctor(nom, prenom, specialite, userId);
        }
    }

    private void addDoctor(String nom, String prenom, String specialite, int userId) {
        String sql = "INSERT INTO doctors (nom, prenom, specialite, user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, specialite);
            ps.setInt(4, userId);
            ps.executeUpdate();
            showMessage("✅ Dr " + prenom + " " + nom + " ajouté avec succès.", true);
            loadDoctors();
            clearForm();
        } catch (SQLException e) {
            showMessage("❌ Erreur : " + e.getMessage(), false);
        }
    }

    private void updateDoctor(int id, String nom, String prenom, String specialite, int userId) {
        String sql = "UPDATE doctors SET nom=?, prenom=?, specialite=?, user_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, specialite);
            ps.setInt(4, userId);
            ps.setInt(5, id);
            ps.executeUpdate();
            showMessage("✅ Médecin mis à jour avec succès.", true);
            loadDoctors();
            clearForm();
        } catch (SQLException e) {
            showMessage("❌ Erreur : " + e.getMessage(), false);
        }
    }

    private void confirmDelete(Doctor d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer Dr « " + d.getPrenom() + " " + d.getNom() + " » ?");
        alert.setContentText("Cette action est irréversible.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) deleteDoctor(d.getId(), d.getNom());
        });
    }

    @FXML
    private void handleDelete() {
        if (!idField.getText().isEmpty()) {
            Doctor d = new Doctor(
                Integer.parseInt(idField.getText()),
                nomField.getText(), prenomField.getText(),
                specialiteField.getText(), 0
            );
            confirmDelete(d);
        }
    }

    private void deleteDoctor(int id, String nom) {
        String sql = "DELETE FROM doctors WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            showMessage("✅ Médecin '" + nom + "' supprimé.", true);
            loadDoctors();
            clearForm();
        } catch (SQLException e) {
            showMessage("❌ Erreur : " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        hideMessage();
        doctorTable.getSelectionModel().clearSelection();
    }

    // ===== HELPERS =====

    private void clearForm() {
        isEditMode = false;
        formTitle.setText("➕ Ajouter un médecin");
        idField.clear();
        nomField.clear();
        prenomField.clear();
        specialiteField.clear();
        if (userIdCombo != null) userIdCombo.setValue(null);
        saveBtn.setText("💾  Enregistrer");
        deleteBtn.setVisible(false);
        deleteBtn.setManaged(false);
        hideMessage();
    }

    private void showMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setStyle(success
            ? "-fx-background-color: rgba(16,185,129,0.1); -fx-text-fill: #059669;" +
              "-fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-font-size: 13px; -fx-font-weight: bold;"
            : "-fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: #dc2626;" +
              "-fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-font-size: 13px; -fx-font-weight: bold;"
        );
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}