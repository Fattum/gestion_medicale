package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private BorderPane mainPane;
    @FXML private Label lblUser;

    // Admin/Secretaire nav buttons
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnMedecins;
    @FXML private Button btnSpecialites;
    @FXML private Button btnPatients;
    @FXML private Button btnRendezVous;
    @FXML private Button btnDossiers;

    // Medecin nav buttons
    @FXML private Button btnMesDisponibilites;
    @FXML private Button btnMesPatientsRdv;
    @FXML private Button btnMesDossiers;
    @FXML private Button btnMesRendezVous;
    @FXML private Button btnMesOrdonnances;

    // Patient nav buttons
    @FXML private Button btnMonDossier;
    @FXML private Button btnMesOrdonnancesPatient;

    @FXML
    public void initialize() {
        var session = SessionManager.getInstance();
        var user = session.getCurrentUser();

        if (user != null) {
            lblUser.setText("Connecté : " + user.getNom() + " (" + user.getRole() + ")");
        }

        // Show/hide buttons based on role
        boolean isAdmin = session.isAdmin();
        boolean isSecretaire = session.isSecretaire();
        boolean isMedecin = session.isMedecin();
        boolean isPatient = session.isPatient();

        if (btnUtilisateurs != null) btnUtilisateurs.setVisible(isAdmin);
        if (btnMedecins != null) btnMedecins.setVisible(isAdmin);
        if (btnSpecialites != null) btnSpecialites.setVisible(isAdmin);
        if (btnPatients != null) btnPatients.setVisible(isAdmin || isSecretaire);
        if (btnRendezVous != null) btnRendezVous.setVisible(isAdmin || isSecretaire);
        if (btnDossiers != null) btnDossiers.setVisible(isAdmin || isSecretaire);

        if (btnMesDisponibilites != null) btnMesDisponibilites.setVisible(isMedecin);
        if (btnMesPatientsRdv != null) btnMesPatientsRdv.setVisible(isMedecin);
        if (btnMesDossiers != null) btnMesDossiers.setVisible(isMedecin);
        if (btnMesRendezVous != null) btnMesRendezVous.setVisible(isMedecin);
        if (btnMesOrdonnances != null) btnMesOrdonnances.setVisible(isMedecin);

        if (btnMonDossier != null) btnMonDossier.setVisible(isPatient);
        if (btnMesOrdonnancesPatient != null) btnMesOrdonnancesPatient.setVisible(isPatient);

        // Load default view
        if (isAdmin) {
            loadView("UserManagement.fxml");
        } else if (isSecretaire) {
            loadView("PatientManagement.fxml");
        } else if (isMedecin) {
            loadView("DisponibiliteManagement.fxml");
        } else if (isPatient) {
            loadView("OrdonnanceManagement.fxml");
        }
    }

    @FXML private void showUtilisateurs() { loadView("UserManagement.fxml"); }
    @FXML private void showMedecins() { loadView("DoctorManagement.fxml"); }
    @FXML private void showSpecialites() { loadView("SpecialiteManagement.fxml"); }
    @FXML private void showPatients() { loadView("PatientManagement.fxml"); }
    @FXML private void showRendezVous() { loadView("RendezVousManagement.fxml"); }
    @FXML private void showDossiers() { loadView("DossierMedicalManagement.fxml"); }
    @FXML private void showMesDisponibilites() { loadView("DisponibiliteManagement.fxml"); }
    @FXML private void showMesPatientsRdv() { loadView("RendezVousManagement.fxml"); }
    @FXML private void showMesDossiers() { loadView("DossierMedicalManagement.fxml"); }
    @FXML private void showMesRendezVous() { loadView("RendezVousManagement.fxml"); }
    @FXML private void showMesOrdonnances() { loadView("OrdonnanceManagement.fxml"); }
    @FXML private void showMonDossier() { loadView("DossierMedicalManagement.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gestion_medicale/login.fxml"));
            Scene scene = new Scene(loader.load(), 450, 350);
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/gestion_medicale/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Gestion Médicale - Connexion");
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gestion_medicale/" + fxmlFile));
            Parent view = loader.load();
            mainPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}