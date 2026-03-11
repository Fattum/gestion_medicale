package com.example.gestion_medicale.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {
    private int id;
    private LocalDate dateRdv;
    private LocalTime heureRdv;
    private String statut;
    private int idPatient;
    private String patientNom;
    private int idMedecin;
    private String medecinNom;
    private Integer idSecretaire;
    private Integer idDisponibilite;

    public RendezVous() {}

    public RendezVous(int id, LocalDate dateRdv, LocalTime heureRdv, String statut,
                      int idPatient, String patientNom, int idMedecin, String medecinNom,
                      Integer idSecretaire, Integer idDisponibilite) {
        this.id = id;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.statut = statut;
        this.idPatient = idPatient;
        this.patientNom = patientNom;
        this.idMedecin = idMedecin;
        this.medecinNom = medecinNom;
        this.idSecretaire = idSecretaire;
        this.idDisponibilite = idDisponibilite;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDateRdv() { return dateRdv; }
    public void setDateRdv(LocalDate dateRdv) { this.dateRdv = dateRdv; }

    public LocalTime getHeureRdv() { return heureRdv; }
    public void setHeureRdv(LocalTime heureRdv) { this.heureRdv = heureRdv; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdPatient() { return idPatient; }
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public int getIdMedecin() { return idMedecin; }
    public void setIdMedecin(int idMedecin) { this.idMedecin = idMedecin; }

    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    public Integer getIdSecretaire() { return idSecretaire; }
    public void setIdSecretaire(Integer idSecretaire) { this.idSecretaire = idSecretaire; }

    public Integer getIdDisponibilite() { return idDisponibilite; }
    public void setIdDisponibilite(Integer idDisponibilite) { this.idDisponibilite = idDisponibilite; }
}