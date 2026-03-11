package com.example.gestion_medicale.models;

public class DossierMedical {
    private int id;
    private int idPatient;
    private String patientNom;
    private String historique;
    private String allergies;
    private String observations;

    public DossierMedical() {}

    public DossierMedical(int id, int idPatient, String patientNom,
                          String historique, String allergies, String observations) {
        this.id = id;
        this.idPatient = idPatient;
        this.patientNom = patientNom;
        this.historique = historique;
        this.allergies = allergies;
        this.observations = observations;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdPatient() { return idPatient; }
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getHistorique() { return historique; }
    public void setHistorique(String historique) { this.historique = historique; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}