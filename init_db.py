import sys

# Try to import a MySQL library
try:
    import pymysql as mysql_lib
    connect = lambda **kw: mysql_lib.connect(**kw)
    lib_name = "pymysql"
except ImportError:
    try:
        import mysql.connector as mysql_lib
        connect = lambda **kw: mysql_lib.connect(**kw)
        lib_name = "mysql.connector"
    except ImportError:
        print("ERROR: No MySQL library found. Install with: pip install pymysql")
        sys.exit(1)

print(f"Using {lib_name}")

SQL_STATEMENTS = [
    "SET FOREIGN_KEY_CHECKS = 0",
    "DROP TABLE IF EXISTS RendezVous, Disponibilite, DossierMedical, Patient, doctors, Medecin, Secretaire, Admin, Specialite, Utilisateur",
    "SET FOREIGN_KEY_CHECKS = 1",
    """CREATE TABLE Utilisateur (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nom VARCHAR(100) NOT NULL,
        motDePasse VARCHAR(255) NOT NULL,
        role ENUM('ADMIN', 'SECRETAIRE', 'MEDECIN') NOT NULL
    ) ENGINE=InnoDB""",
    """CREATE TABLE Specialite (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nom VARCHAR(100) NOT NULL,
        description TEXT
    ) ENGINE=InnoDB""",
    """CREATE TABLE Admin (
        id_utilisateur INT PRIMARY KEY,
        FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE
    ) ENGINE=InnoDB""",
    """CREATE TABLE Secretaire (
        id_utilisateur INT PRIMARY KEY,
        FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE
    ) ENGINE=InnoDB""",
    """CREATE TABLE Medecin (
        id_utilisateur INT PRIMARY KEY,
        id_specialite INT,
        FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE,
        FOREIGN KEY (id_specialite) REFERENCES Specialite(id) ON DELETE SET NULL
    ) ENGINE=InnoDB""",
    """CREATE TABLE Disponibilite (
        id INT AUTO_INCREMENT PRIMARY KEY,
        date_dispo DATE NOT NULL,
        heureDebut TIME NOT NULL,
        heureFin TIME NOT NULL,
        id_medecin INT NOT NULL,
        FOREIGN KEY (id_medecin) REFERENCES Medecin(id_utilisateur) ON DELETE CASCADE
    ) ENGINE=InnoDB""",
    """CREATE TABLE Patient (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nom VARCHAR(100) NOT NULL,
        telephone VARCHAR(20),
        adresse TEXT
    ) ENGINE=InnoDB""",
    """CREATE TABLE DossierMedical (
        id INT AUTO_INCREMENT PRIMARY KEY,
        id_patient INT NOT NULL UNIQUE,
        historique TEXT,
        allergies TEXT,
        observations TEXT,
        FOREIGN KEY (id_patient) REFERENCES Patient(id) ON DELETE CASCADE
    ) ENGINE=InnoDB""",
    """CREATE TABLE RendezVous (
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
    ) ENGINE=InnoDB""",
    "INSERT INTO Utilisateur (nom, motDePasse, role) VALUES ('admin', 'admin123', 'ADMIN')",
    "INSERT INTO Admin (id_utilisateur) VALUES (1)",
    "INSERT INTO Specialite (nom, description) VALUES ('Cardiologie', 'Specialite du coeur et du systeme cardiovasculaire')",
    "INSERT INTO Specialite (nom, description) VALUES ('Dermatologie', 'Specialite de la peau')",
    "INSERT INTO Specialite (nom, description) VALUES ('Neurologie', 'Specialite du systeme nerveux')",
    "INSERT INTO Specialite (nom, description) VALUES ('Pediatrie', 'Specialite des enfants')",
    "INSERT INTO Specialite (nom, description) VALUES ('Generaliste', 'Medecine generale')",
]

try:
    conn = connect(
        host="ballast.proxy.rlwy.net",
        port=49937,
        user="root",
        password="BjRiAtgFXwPGefkletBgNAFyHFaoBcIU",
        database="gestion_medical",
        ssl_disabled=True
    )
    cursor = conn.cursor()
    for sql in SQL_STATEMENTS:
        sql = sql.strip()
        if sql:
            try:
                cursor.execute(sql)
                conn.commit()
                print(f"OK: {sql[:60]}...")
            except Exception as e:
                print(f"WARN: {e} -> {sql[:60]}")
    
    # Verify
    cursor.execute("SHOW TABLES")
    tables = [row[0] for row in cursor.fetchall()]
    print(f"\nTables created: {tables}")

    cursor.execute("SELECT id, nom, role FROM Utilisateur")
    users = cursor.fetchall()
    print(f"Users: {users}")

    cursor.close()
    conn.close()
    print("\nDatabase initialized successfully!")
except Exception as e:
    print(f"Connection error: {e}")
    sys.exit(1)