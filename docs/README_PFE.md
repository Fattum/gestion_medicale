## Documentation – Projet `gestion_medicale`

### Objectif
Application JavaFX (Java 17 / JavaFX 21) de gestion médicale avec rôles **ADMIN**, **SECRETAIRE**, **MEDECIN**, **PATIENT**.
Fonctionnalités principales : gestion des utilisateurs/médecins/spécialités/patients, rendez-vous, disponibilités, dossiers médicaux, **ordonnances** (création médecin + consultation patient).

### Stack technique
- **UI** : JavaFX + FXML + CSS
- **Accès DB** : JDBC
- **Pool DB** : HikariCP
- **BD** : MySQL
- **Concurrence** : `ExecutorService` + JavaFX `Task` (chargements asynchrones sur l’écran Rendez-vous)
- **Collections** : `ObservableList` (JavaFX), `HashMap` (indexation par id), Streams/Collectors

---

## Structure des dossiers

### `src/main/java/com/example/gestion_medicale/`
- **`Main.java`** : point d’entrée JavaFX. Initialise la base via `DatabaseInitializer.initialize()` puis lance l’écran `login.fxml`. Ajoute aussi des hooks de fermeture pour arrêter proprement HikariCP et le pool de threads.
- **`DatabaseConnection.java`** : initialise **HikariCP** à partir de `database.properties` et fournit `getConnection()`.
- **`DatabaseInitializer.java`** : lit `init_database.sql` (resources) et exécute les statements pour créer/initialiser la base.
- **`DbInitRunner.java`** : utilitaire d’exécution (si utilisé) pour init DB.
- **`SessionManager.java`** : stocke l’utilisateur connecté (`currentUser`) + `currentPatientId` pour filtrer l’accès patient.
- **`AppExecutors.java`** : pool de threads (`ExecutorService`) utilisé pour exécuter des tâches DB en arrière‑plan.

### `src/main/java/com/example/gestion_medicale/models/`
POJOs (entités) pour transporter les données entre DB ↔ contrôleurs ↔ UI.
- **`User`** : compte (id, nom, motDePasse, role).
- **`Patient`** : patient (id, nom, telephone, adresse).
- **`Doctor`** : médecin (id utilisateur + spécialité).
- **`Specialite`** : spécialité médicale.
- **`Disponibilite`** : disponibilité médecin (date, heure début/fin).
- **`RendezVous`** : rendez-vous (date/heure/statut + ids patient/médecin).
- **`DossierMedical`** : dossier (historique/allergies/observations) lié 1–1 à patient.
- **`Ordonnance`** : ordonnance liée à un rendez‑vous + patient + médecin (libellé, contenu, date).

### `src/main/java/com/example/gestion_medicale/controllers/`
Contrôleurs JavaFX (logique UI + JDBC).
- **`LoginController`** : connexion. Si rôle PATIENT, récupère `id_patient` via `PatientCompte` et le met en session.
- **`MainController`** : menu/navigation. Affiche/masque boutons selon le rôle. Charge les vues (FXML) au centre.
- **`UserController`** : CRUD utilisateurs. Rôle **PATIENT** disponible + liaison à un patient via `PatientCompte`.
- **`PatientController`** : CRUD patients. À l’ajout, crée automatiquement le `DossierMedical`.
- **`DoctorController`** : CRUD médecins et liaison à une spécialité.
- **`SpecialiteController`** : CRUD spécialités.
- **`DisponibiliteController`** : gestion des disponibilités.
- **`RendezVousController`** : gestion RDV. Charge patients/médecins/RDV **en asynchrone** (Task + ExecutorService) et utilise `HashMap` pour indexer patients/médecins par id.
- **`DossierMedicalController`** : affiche/édite dossiers. Pour un patient connecté, limite à son dossier. Affiche la liste des ordonnances du patient dans le dossier.
- **`OrdonnanceController`** : médecin crée une ordonnance depuis un RDV; patient consulte ses ordonnances.

---

## Structure UI (FXML)
Chemin : `src/main/resources/com/example/gestion_medicale/`
- **`login.fxml`** : écran de connexion.
- **`MainView.fxml`** : layout principal + sidebar navigation.
- **`UserManagement.fxml`** : gestion utilisateurs.
- **`PatientManagement.fxml`** : gestion patients.
- **`DoctorManagement.fxml`** : gestion médecins.
- **`SpecialiteManagement.fxml`** : gestion spécialités.
- **`DisponibiliteManagement.fxml`** : disponibilités.
- **`RendezVousManagement.fxml`** : rendez-vous.
- **`DossierMedicalManagement.fxml`** : dossiers + ordonnances du patient.
- **`OrdonnanceManagement.fxml`** : ordonnances (création/consultation).
- **`styles.css`** : style global.

---

## Schéma base de données (résumé)
Script : `src/main/resources/init_database.sql`

### Tables principales
- **`Utilisateur`** : comptes (roles : ADMIN/SECRETAIRE/MEDECIN/PATIENT)
- **`Admin`**, **`Secretaire`**, **`Medecin`** : tables “rôles” (héritage logique).
- **`Patient`** : fiche patient
- **`PatientCompte`** : lien 1–1 entre `Utilisateur(PATIENT)` et `Patient`
- **`DossierMedical`** : dossier 1–1 patient
- **`Disponibilite`** : disponibilités médecin
- **`RendezVous`** : rendez-vous patient↔médecin
- **`Ordonnance`** : ordonnances liées à un RDV + patient + médecin

---

## Où le cours est utilisé (résumé)

### Collections
- **List** : `ObservableList` + `FXCollections.observableArrayList()` (tous contrôleurs UI).
- **Map** : `HashMap` dans `RendezVousController` (indexation `id -> Patient/Doctor`).
- **Streams / Collectors** : `DatabaseInitializer` (`lines().collect(joining)`), filtres via `.stream().filter(...).findFirst()...` (plusieurs contrôleurs).

### Threads
- **ExecutorService (thread pool)** : `AppExecutors`.
- **JavaFX Task** : chargements asynchrones dans `RendezVousController`.
- **synchronized** : `SessionManager.getInstance()` synchronisé.

---

## Comment tester rapidement
1. Configurer `src/main/resources/database.properties`
2. Lancer : `mvn javafx:run`
3. Créer un patient, puis dans Utilisateurs créer un compte rôle **PATIENT** et le lier.

