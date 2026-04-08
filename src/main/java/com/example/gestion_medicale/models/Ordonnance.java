package com.example.gestion_medicale.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Ordonnance {
    private int id;
    private int idRdv;
    private LocalDate dateRdv;
    private String heureRdv;
    private int idPatient;
    private String patientNom;
    private int idMedecin;
    private String medecinNom;
    private String libelle;
    private String contenu;
    private LocalDate dateOrdonnance;
    private LocalDateTime createdAt;

    public Ordonnance() {}

    public Ordonnance(int id, int idRdv, LocalDate dateRdv, String heureRdv,
                      int idPatient, String patientNom,
                      int idMedecin, String medecinNom,
                      String libelle, String contenu,
                      LocalDate dateOrdonnance, LocalDateTime createdAt) {
        this.id = id;
        this.idRdv = idRdv;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.idPatient = idPatient;
        this.patientNom = patientNom;
        this.idMedecin = idMedecin;
        this.medecinNom = medecinNom;
        this.libelle = libelle;
        this.contenu = contenu;
        this.dateOrdonnance = dateOrdonnance;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdRdv() { return idRdv; }
    public void setIdRdv(int idRdv) { this.idRdv = idRdv; }

    public LocalDate getDateRdv() { return dateRdv; }
    public void setDateRdv(LocalDate dateRdv) { this.dateRdv = dateRdv; }

    public String getHeureRdv() { return heureRdv; }
    public void setHeureRdv(String heureRdv) { this.heureRdv = heureRdv; }

    public int getIdPatient() { return idPatient; }
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public int getIdMedecin() { return idMedecin; }
    public void setIdMedecin(int idMedecin) { this.idMedecin = idMedecin; }

    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDate getDateOrdonnance() { return dateOrdonnance; }
    public void setDateOrdonnance(LocalDate dateOrdonnance) { this.dateOrdonnance = dateOrdonnance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

