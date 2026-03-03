package com.example.gestion_medicale.models;

/**
 * Modèle représentant un utilisateur de l'application.
 *
 * <p>Correspond à la table {@code users} en base de données :</p>
 * <pre>
 * CREATE TABLE IF NOT EXISTS users (
 *     id       INT AUTO_INCREMENT PRIMARY KEY,
 *     username VARCHAR(100) NOT NULL UNIQUE,
 *     password VARCHAR(255) NOT NULL,
 *     role     ENUM('admin','secretary') NOT NULL DEFAULT 'secretary'
 * );
 * </pre>
 */
public class User {

    /** Identifiant unique (clé primaire). */
    private int id;

    /** Nom d'utilisateur (unique). */
    private String username;

    /** Mot de passe (stocké tel quel ou hashé selon l'implémentation). */
    private String password;

    /**
     * Rôle de l'utilisateur.
     * Valeurs possibles : {@code "admin"} ou {@code "secretary"}.
     */
    private String role;

    // ── Constructeurs ────────────────────────────────────────────────────────

    /** Constructeur sans arguments (requis par certains frameworks). */
    public User() {}

    /**
     * Constructeur complet.
     *
     * @param id       identifiant
     * @param username nom d'utilisateur
     * @param password mot de passe
     * @param role     rôle ({@code "admin"} ou {@code "secretary"})
     */
    public User(int id, String username, String password, String role) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    /**
     * Constructeur sans id (utilisé pour la création d'un nouvel utilisateur).
     *
     * @param username nom d'utilisateur
     * @param password mot de passe
     * @param role     rôle
     */
    public User(String username, String password, String role) {
        this(0, username, password, role);
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "User{id=" + id
                + ", username='" + username + '\''
                + ", role='" + role + '\''
                + '}';
    }
}