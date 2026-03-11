package com.example.gestion_medicale.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Disponibilite {
    private int id;
    private LocalDate dateDispo;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private int idMedecin;
    private String medecinNom;

    public Disponibilite() {}

    public Disponibilite(int id, LocalDate dateDispo, LocalTime heureDebut,
                         LocalTime heureFin, int idMedecin, String medecinNom) {
        this.id = id;
        this.dateDispo = dateDispo;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.idMedecin = idMedecin;
        this.medecinNom = medecinNom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDateDispo() { return dateDispo; }
    public void setDateDispo(LocalDate dateDispo) { this.dateDispo = dateDispo; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public int getIdMedecin() { return idMedecin; }
    public void setIdMedecin(int idMedecin) { this.idMedecin = idMedecin; }

    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    @Override
    public String toString() {
        return dateDispo + " " + heureDebut + " - " + heureFin;
    }
}