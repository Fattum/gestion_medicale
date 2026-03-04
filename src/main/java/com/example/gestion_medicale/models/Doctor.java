package com.example.gestion_medicale.models;
public class Doctor {
    private int id;
    private String nom;
    private String prenom;
    private String specialite;
    private int userId;
    public Doctor() {}
    public Doctor(int id, String nom, String prenom, String specialite, int userId) {
        this.id         = id;
        this.nom        = nom;
        this.prenom     = prenom;
        this.specialite = specialite;
        this.userId     = userId;
    }
    public Doctor(String nom, String prenom, String specialite, int userId) {
        this(0, nom, prenom, specialite, userId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    @Override
    public String toString() {
        return "Doctor{id=" + id
                + ", nom='" + nom + '\''
                + ", prenom='" + prenom + '\''
                + ", specialite='" + specialite + '\''
                + ", userId=" + userId
                + '}';
    }
}