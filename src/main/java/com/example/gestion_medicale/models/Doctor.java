package com.example.gestion_medicale.models;

public class Doctor extends User {
    private int idSpecialite;
    private String nomSpecialite;

    public Doctor() {}

    public Doctor(int id, String nom, String motDePasse, int idSpecialite, String nomSpecialite) {
        super(id, nom, motDePasse, "MEDECIN");
        this.idSpecialite = idSpecialite;
        this.nomSpecialite = nomSpecialite;
    }

    public int getIdSpecialite() { return idSpecialite; }
    public void setIdSpecialite(int idSpecialite) { this.idSpecialite = idSpecialite; }

    public String getNomSpecialite() { return nomSpecialite; }
    public void setNomSpecialite(String nomSpecialite) { this.nomSpecialite = nomSpecialite; }

    @Override
    public String toString() {
        return "Dr. " + getNom() + (nomSpecialite != null ? " - " + nomSpecialite : "");
    }
}