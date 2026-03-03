-- ============================================================
-- Script d'initialisation de la base de données gestion_medical
-- À exécuter une seule fois sur la base Railway MySQL.
-- ============================================================

-- Table des utilisateurs
-- Rôles disponibles : admin, secretary, doctor
CREATE TABLE IF NOT EXISTS users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     ENUM('admin','secretary','doctor') NOT NULL DEFAULT 'secretary'
);

-- Table des médecins
CREATE TABLE IF NOT EXISTS doctors (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nom        VARCHAR(100) NOT NULL,
    prenom     VARCHAR(100) NOT NULL,
    specialite VARCHAR(150),
    user_id    INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ── Données initiales ────────────────────────────────────────────────────────

-- Compte administrateur par défaut  → login : admin      / admin123
INSERT IGNORE INTO users (username, password, role)
VALUES ('admin', 'admin123', 'admin');

-- Compte secrétaire par défaut      → login : secretaire / secret123
INSERT IGNORE INTO users (username, password, role)
VALUES ('secretaire', 'secret123', 'secretary');

-- Compte médecin par défaut         → login : docteur    / doctor123
INSERT IGNORE INTO users (username, password, role)
VALUES ('docteur', 'doctor123', 'doctor');

-- Médecins d'exemple
INSERT IGNORE INTO doctors (nom, prenom, specialite, user_id)
VALUES ('Martin',  'Sophie',  'Cardiologue',  NULL),
       ('Bernard', 'Pierre',  'Généraliste',  NULL),
       ('Durand',  'Marie',   'Pédiatre',     NULL);