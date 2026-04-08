SET FOREIGN_KEY_CHECKS = 0;

-- Nettoyage de la base (includes legacy table names)
DROP TABLE IF EXISTS Ordonnance, PatientCompte, RendezVous, Disponibilite, DossierMedical, Patient, doctors, Medecin, Secretaire, Admin, Specialite, Utilisateur;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Table Utilisateur
CREATE TABLE Utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    motDePasse VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'SECRETAIRE', 'MEDECIN', 'PATIENT') NOT NULL
) ENGINE=InnoDB;

-- 2. Table Specialite
CREATE TABLE Specialite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description TEXT
) ENGINE=InnoDB;

-- 3. Tables héritées
CREATE TABLE Admin (
    id_utilisateur INT PRIMARY KEY,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Secretaire (
    id_utilisateur INT PRIMARY KEY,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Medecin (
    id_utilisateur INT PRIMARY KEY,
    id_specialite INT,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (id_specialite) REFERENCES Specialite(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Table Disponibilite
CREATE TABLE Disponibilite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_dispo DATE NOT NULL,
    heureDebut TIME NOT NULL,
    heureFin TIME NOT NULL,
    id_medecin INT NOT NULL,
    FOREIGN KEY (id_medecin) REFERENCES Medecin(id_utilisateur) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Table Patient
CREATE TABLE Patient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    adresse TEXT
) ENGINE=InnoDB;

-- 5b. Liaison Compte Patient <-> Patient
-- Un utilisateur avec rôle PATIENT correspond à exactement un patient
CREATE TABLE PatientCompte (
    id_utilisateur INT PRIMARY KEY,
    id_patient INT NOT NULL UNIQUE,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (id_patient) REFERENCES Patient(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Table DossierMedical (Composition avec Patient)
CREATE TABLE DossierMedical (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_patient INT NOT NULL UNIQUE,
    historique TEXT,
    allergies TEXT,
    observations TEXT,
    FOREIGN KEY (id_patient) REFERENCES Patient(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Table RendezVous
CREATE TABLE RendezVous (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_rdv DATE NOT NULL,
    heure_rdv TIME NOT NULL,
    statut ENUM('PLANIFIE', 'CONFIRME', 'ANNULE', 'TERMINE') DEFAULT 'PLANIFIE',
    id_patient INT NOT NULL,
    id_medecin INT NOT NULL,
    id_secretaire INT,
    id_disponibilite INT,
    FOREIGN KEY (id_patient) REFERENCES Patient(id) ON DELETE CASCADE,
    FOREIGN KEY (id_medecin) REFERENCES Medecin(id_utilisateur) ON DELETE CASCADE,
    FOREIGN KEY (id_secretaire) REFERENCES Secretaire(id_utilisateur) ON DELETE SET NULL,
    FOREIGN KEY (id_disponibilite) REFERENCES Disponibilite(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 8. Table Ordonnance (liée à un RDV + patient + médecin)
CREATE TABLE Ordonnance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_rdv INT NOT NULL,
    id_patient INT NOT NULL,
    id_medecin INT NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    contenu TEXT,
    date_ordonnance DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rdv) REFERENCES RendezVous(id) ON DELETE CASCADE,
    FOREIGN KEY (id_patient) REFERENCES Patient(id) ON DELETE CASCADE,
    FOREIGN KEY (id_medecin) REFERENCES Medecin(id_utilisateur) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Données initiales
INSERT INTO Utilisateur (nom, motDePasse, role) VALUES ('admin', 'admin123', 'ADMIN');
INSERT INTO Admin (id_utilisateur) VALUES (1);

INSERT INTO Specialite (nom, description) VALUES
    ('Cardiologie', 'Spécialité du cœur et du système cardiovasculaire'),
    ('Dermatologie', 'Spécialité de la peau'),
    ('Neurologie', 'Spécialité du système nerveux'),
    ('Pédiatrie', 'Spécialité des enfants'),
    ('Généraliste', 'Médecine générale');