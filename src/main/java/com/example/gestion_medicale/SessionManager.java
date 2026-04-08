package com.example.gestion_medicale;

import com.example.gestion_medicale.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Integer currentPatientId;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public Integer getCurrentPatientId() { return currentPatientId; }
    public void setCurrentPatientId(Integer currentPatientId) { this.currentPatientId = currentPatientId; }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public boolean isSecretaire() {
        return currentUser != null && "SECRETAIRE".equals(currentUser.getRole());
    }

    public boolean isMedecin() {
        return currentUser != null && "MEDECIN".equals(currentUser.getRole());
    }

    public boolean isPatient() {
        return currentUser != null && "PATIENT".equals(currentUser.getRole());
    }

    public void logout() {
        currentUser = null;
        currentPatientId = null;
    }
}