package com.example.gestion_medicale.controllers;

import com.example.gestion_medicale.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {

    @FXML private BorderPane mainPane;
    @FXML private Label lblUser;
    @FXML private Label lblSidebarUser;

    @FXML private VBox sectionAdmin;
    @FXML private VBox sectionGestion;
    @FXML private VBox sectionMedecin;
    @FXML private VBox sectionPatient;
    @FXML private Separator sepGestion;
    @FXML private Separator sepMedecin;
    @FXML private Separator sepPatient;

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

    private Button activeNavButton;

    @FXML
    public void initialize() {
        var session = SessionManager.getInstance();
        var user = session.getCurrentUser();

        if (user != null) {
            lblUser.setText("Connecté : " + user.getNom() + " (" + user.getRole() + ")");
            if (lblSidebarUser != null) {
                lblSidebarUser.setText(user.getNom() + " • " + user.getRole());
            }
        }

        // Show/hide buttons based on role
        boolean isAdmin = session.isAdmin();
        boolean isSecretaire = session.isSecretaire();
        boolean isMedecin = session.isMedecin();
        boolean isPatient = session.isPatient();

        setNavVisible(btnUtilisateurs, isAdmin);
        setNavVisible(btnMedecins, isAdmin);
        setNavVisible(btnSpecialites, isAdmin);

        setNavVisible(btnPatients, isAdmin || isSecretaire);
        setNavVisible(btnRendezVous, isAdmin || isSecretaire);
        setNavVisible(btnDossiers, isAdmin || isSecretaire);

        setNavVisible(btnMesDisponibilites, isMedecin);
        setNavVisible(btnMesPatientsRdv, isMedecin);
        setNavVisible(btnMesDossiers, isMedecin);
        setNavVisible(btnMesRendezVous, isMedecin);
        setNavVisible(btnMesOrdonnances, isMedecin);

        setNavVisible(btnMonDossier, isPatient);
        setNavVisible(btnMesOrdonnancesPatient, isPatient);

        // Hide empty sections (and their separators) so the sidebar doesn't look "broken"
        updateSectionVisibility();

        // Load default view
        if (isAdmin) {
            loadView("UserManagement.fxml");
            setActive(btnUtilisateurs);
        } else if (isSecretaire) {
            loadView("PatientManagement.fxml");
            setActive(btnPatients);
        } else if (isMedecin) {
            loadView("DisponibiliteManagement.fxml");
            setActive(btnMesDisponibilites);
        } else if (isPatient) {
            loadView("OrdonnanceManagement.fxml");
            setActive(btnMesOrdonnancesPatient);
        }
    }

    @FXML private void showUtilisateurs() { loadView("UserManagement.fxml"); setActive(btnUtilisateurs); }
    @FXML private void showMedecins() { loadView("DoctorManagement.fxml"); setActive(btnMedecins); }
    @FXML private void showSpecialites() { loadView("SpecialiteManagement.fxml"); setActive(btnSpecialites); }
    @FXML private void showPatients() { loadView("PatientManagement.fxml"); setActive(btnPatients); }
    @FXML private void showRendezVous() { loadView("RendezVousManagement.fxml"); setActive(btnRendezVous); }
    @FXML private void showDossiers() { loadView("DossierMedicalManagement.fxml"); setActive(btnDossiers); }
    @FXML private void showMesDisponibilites() { loadView("DisponibiliteManagement.fxml"); setActive(btnMesDisponibilites); }
    @FXML private void showMesPatientsRdv() { loadView("RendezVousManagement.fxml"); setActive(btnMesPatientsRdv != null ? btnMesPatientsRdv : btnMesRendezVous); }
    @FXML private void showMesDossiers() { loadView("DossierMedicalManagement.fxml"); setActive(btnMesDossiers); }
    @FXML private void showMesRendezVous() { loadView("RendezVousManagement.fxml"); setActive(btnMesRendezVous); }
    @FXML private void showMesOrdonnances() { loadView("OrdonnanceManagement.fxml"); setActive(btnMesOrdonnances); }
    @FXML private void showMonDossier() { loadView("DossierMedicalManagement.fxml"); setActive(btnMonDossier); }

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

    private void setNavVisible(Button btn, boolean visible) {
        if (btn == null) return;
        btn.setVisible(visible);
        btn.setManaged(visible);
    }

    private void updateSectionVisibility() {
        setSectionVisible(sectionAdmin, hasAnyVisible(btnUtilisateurs, btnMedecins, btnSpecialites));
        setSectionVisible(sectionGestion, hasAnyVisible(btnPatients, btnRendezVous, btnDossiers));
        setSectionVisible(sectionMedecin, hasAnyVisible(btnMesDisponibilites, btnMesPatientsRdv, btnMesDossiers, btnMesRendezVous, btnMesOrdonnances));
        setSectionVisible(sectionPatient, hasAnyVisible(btnMonDossier, btnMesOrdonnancesPatient));

        // Only show separators that precede a visible section
        setSepVisible(sepGestion, isSectionVisible(sectionGestion) && isAnyPreviousSectionVisible(sectionAdmin));
        setSepVisible(sepMedecin, isSectionVisible(sectionMedecin) && isAnyPreviousSectionVisible(sectionAdmin, sectionGestion));
        setSepVisible(sepPatient, isSectionVisible(sectionPatient) && isAnyPreviousSectionVisible(sectionAdmin, sectionGestion, sectionMedecin));
    }

    private void setSectionVisible(VBox section, boolean visible) {
        if (section == null) return;
        section.setVisible(visible);
        section.setManaged(visible);
    }

    private void setSepVisible(Separator sep, boolean visible) {
        if (sep == null) return;
        sep.setVisible(visible);
        sep.setManaged(visible);
    }

    private boolean hasAnyVisible(Button... buttons) {
        if (buttons == null) return false;
        for (Button b : buttons) {
            if (b != null && b.isVisible()) return true;
        }
        return false;
    }

    private boolean isSectionVisible(VBox section) {
        return section != null && section.isVisible();
    }

    private boolean isAnyPreviousSectionVisible(VBox... sections) {
        if (sections == null) return false;
        for (VBox s : sections) {
            if (s != null && s.isVisible()) return true;
        }
        return false;
    }

    private void setActive(Button btn) {
        if (btn == null) return;
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-btn-active");
        }
        activeNavButton = btn;
        if (!activeNavButton.getStyleClass().contains("nav-btn-active")) {
            activeNavButton.getStyleClass().add("nav-btn-active");
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