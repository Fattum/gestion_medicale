package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.DatabaseConnection;
import com.example.gestion_medicale.models.User;
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
public class UserController implements Initializable {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colUsername;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, Void>    colActions;
    @FXML private TextField searchField;
    @FXML private Label     countLabel;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField     idField;
    @FXML private Label         formTitle;
    @FXML private Label         messageLabel;
    @FXML private Button        saveBtn;
    @FXML private Button        deleteBtn;

    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private boolean isEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleCombo.getItems().addAll("admin", "secretary");
        setupTable();
        loadUsers();
        clearForm();
    }
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null); setText(null);
                } else {
                    Label badge = new Label(role);
                    if ("admin".equals(role)) {
                        badge.setStyle(
                            "-fx-background-color: rgba(99,102,241,0.15);" +
                            "-fx-text-fill: #6366f1;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 3 10 3 10;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;"
                        );
                    } else {
                        badge.setStyle(
                            "-fx-background-color: rgba(16,185,129,0.15);" +
                            "-fx-text-fill: #10b981;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 3 10 3 10;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;"
                        );
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏ Modifier");
            private final Button delBtn    = new Button("🗑 Supprimer");
            private final HBox   box       = new HBox(6, editBtn, delBtn);

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
                    User u = getTableView().getItems().get(getIndex());
                    populateForm(u);
                });
                delBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    confirmDelete(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        userTable.setItems(userList);
    }

    private void loadUsers() {
        userList.clear();
        String sql = "SELECT id, username, role FROM users ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    "",
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            showMessage("Erreur de chargement : " + e.getMessage(), false);
        }
        updateCount();
    }

    private void updateCount() {
        countLabel.setText(userList.size() + " utilisateur(s)");
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            userTable.setItems(userList);
            countLabel.setText(userList.size() + " utilisateur(s)");
            return;
        }
        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User u : userList) {
            if (u.getUsername().toLowerCase().contains(q) ||
                u.getRole().toLowerCase().contains(q)) {
                filtered.add(u);
            }
        }
        userTable.setItems(filtered);
        countLabel.setText(filtered.size() + " résultat(s)");
    }

    @FXML
    private void showAddForm() {
        clearForm();
        isEditMode = false;
        formTitle.setText("➕ Ajouter un utilisateur");
        saveBtn.setText("💾  Enregistrer");
        deleteBtn.setVisible(false);
        deleteBtn.setManaged(false);
    }

    @FXML
    private void onTableRowSelected() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) populateForm(selected);
    }

    private void populateForm(User u) {
        isEditMode = true;
        formTitle.setText("✏ Modifier l'utilisateur");
        idField.setText(String.valueOf(u.getId()));
        usernameField.setText(u.getUsername());
        passwordField.clear();
        roleCombo.setValue(u.getRole());
        saveBtn.setText("💾  Mettre à jour");
        deleteBtn.setVisible(true);
        deleteBtn.setManaged(true);
        hideMessage();
    }

    @FXML
    private void handleSave() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role     = roleCombo.getValue();

        if (username.isEmpty() || role == null) {
            showMessage("⚠ Veuillez remplir tous les champs obligatoires.", false);
            return;
        }
        if (!isEditMode && password.isEmpty()) {
            showMessage("⚠ Le mot de passe est requis pour un nouvel utilisateur.", false);
            return;
        }

        if (isEditMode) {
            updateUser(Integer.parseInt(idField.getText()), username, password, role);
        } else {
            addUser(username, password, role);
        }
    }

    private void addUser(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
            showMessage("✅ Utilisateur '" + username + "' ajouté avec succès.", true);
            loadUsers();
            clearForm();
        } catch (SQLException e) {
            showMessage("❌ Erreur : " + e.getMessage(), false);
        }
    }

    private void updateUser(int id, String username, String password, String role) {
        String sql = password.isEmpty()
            ? "UPDATE users SET username=?, role=? WHERE id=?"
            : "UPDATE users SET username=?, password=?, role=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (password.isEmpty()) {
                ps.setString(1, username);
                ps.setString(2, role);
                ps.setInt(3, id);
            } else {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, role);
                ps.setInt(4, id);
            }
            ps.executeUpdate();
            showMessage("✅ Utilisateur mis à jour avec succès.", true);
            loadUsers();
            clearForm();
        } catch (SQLException e) {
            showMessage("❌ Erreur : " + e.getMessage(), false);
        }
    }

    private void confirmDelete(User u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer l'utilisateur « " + u.getUsername() + " » ?");
        alert.setContentText("Cette action est irréversible.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) deleteUser(u.getId(), u.getUsername());
        });
    }

    @FXML
    private void handleDelete() {
        if (!idField.getText().isEmpty()) {
            String username = usernameField.getText();
            confirmDelete(new User(Integer.parseInt(idField.getText()), username, "", ""));
        }
    }

    private void deleteUser(int id, String username) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            showMessage("✅ Utilisateur '" + username + "' supprimé.", true);
            loadUsers();
            clearForm();
        } catch (SQLException e) {
            showMessage("❌ Erreur : " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        hideMessage();
        userTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        isEditMode = false;
        formTitle.setText("➕ Ajouter un utilisateur");
        idField.clear();
        usernameField.clear();
        passwordField.clear();
        if (roleCombo != null) roleCombo.setValue(null);
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