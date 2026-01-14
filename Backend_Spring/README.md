# Backend_Spring - Student-Focused PFA Management API

## Quick Start

This is a **Spring Boot 3.5.9** REST API server providing Student-only access to the PFA (Projet de Fin d'Année) management system.

### Build & Run

```bash
# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run

# Default server: http://localhost:8080
```

### Database Configuration

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pfa_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=create-drop  # or update for production
```

---

## Project Structure

```
src/main/java/com/ensate/pfa/
├── controller/              # REST endpoints
├── service/                 # Service interfaces + implementations
├── entity/                  # JPA entities
├── repository/              # Spring Data JPA repositories
├── dto/                     # Request/Response DTOs
└── exception/               # Global error handling
```

---

**Framework:** Spring Boot 3.5.9  
**Language:** Java 17  
**Build Tool:** Maven  

---

## Use Cases par Acteur

### 1. Etudiant (Student)

* **Demander convention de stage** (Request internship convention)
* **Consulter la convention** (Consult the convention)
* *Extension:* **Deposer convention signee** (Upload signed convention)

* **Deposer les livrables** (Deposit deliverables)
* *Specialization:* **Deposer les livrables avant soutenance** (Deposit deliverables before defense)
* *Specialization:* **Deposer les livrables après soutenance** (Deposit deliverables after defense)

* **Consulter date de soutenance** (Consult defense date)
* **Recevoir les notifications** (Receive notifications)
* **Consulter les évaluations** (Consult evaluations)

### 2. Administrateur (Administrator)

* **Traiter les demandes de convention** (Process convention requests)
* *Include:* **Deposer la convention** (Deposit the convention)

* **Consulter les conventions signées déposées** (Consult signed deposited conventions)
* **Gerer les utilisateurs** (Manage users)

### 3. Coordinateur du filiere (Coordinator)

* **Affecter les encardants aux etudiants** (Assign supervisors to students)
* **Plannifier les soutenances** (Plan defenses)
* *Extension:* **Affecter les jurry** (Assign jury)

* **Superviser les évaluation** (Supervise evaluations)

### 4. Encadrant (Supervisor)

* **Consulter la liste des etudiants a encadrer** (Consult list of students to supervise)
* **Valider les livrables** (Validate deliverables)
* **Consulter les notifications** (Consult notifications)
* **Evaluer un PFA** (Evaluate a PFA)

### 5. Shared / System Utility Use Cases

* **S'authentifier** (Authenticate) – *Included by almost all main use cases.*
* **Envoyer des notifications** (Send notifications) – *Included by "Demander convention de stage" and "Recevoir les notifications".*
* **Calculer la note finale** (Calculate final grade) – *Included by "Evaluer un PFA" and "Superviser les évaluation".*

---

## API Documentation

### Student Endpoints

#### 1. PFA Dossier Management

**Use Case: Créer ou récupérer un dossier PFA (Create or retrieve PFA dossier)**
- **API Endpoint:** `POST /api/pfa-dossiers/create-or-get`
- **Method:** `PFADossierService.createOrGetDossier(PFADossierRequest request)`
- **Request DTO:** `PFADossierRequest`
  - Fields: `studentId`, `title`, `description`, `supervisorId` (optional)
- **Response DTO:** `PFADossierResponse`
- **Description:** This is STEP 1 before requesting a convention. Students create a new PFA dossier or reuse an existing active one.

**Use Case: Consulter les dossiers PFA (Consult PFA dossiers)**
- **API Endpoints:**
  - `GET /api/pfa-dossiers/{id}` - Get specific dossier
  - `GET /api/pfa-dossiers/student/{studentId}` - List all student's dossiers
- **Description:** Students query their PFA dossiers to view project status, linked convention, and metadata.

---

#### 2. Convention Management

**Use Case: Demander convention de stage (Request internship convention)**
- **API Endpoint:** `POST /api/conventions`
- **Method:** `ConventionService.requestConvention(ConventionRequest request)`
- **Request DTO:** `ConventionRequest`
  - Fields: `studentId`, `pfaId`, `companyName`, `companyAddress`, `companySupervisorName`, `companySupervisorEmail`, `startDate`, `endDate`
- **Response DTO:** `ConventionResponse`
- **Description:** STEP 2 after dossier creation. Students request a convention for their internship placement.

**Use Case: Consulter la convention (Consult convention)**
- **API Endpoints:**
  - `GET /api/conventions/{id}` - Get convention details
  - `GET /api/conventions/pfa/{pfaId}` - Get convention for PFA
- **Description:** Students view their convention details, including company information, dates, and signed document URI.

**Use Case: Deposer convention signee (Upload signed convention)**
- **API Endpoint:** `POST /api/conventions/{id}/upload-signed?scannedFileUri=<uri>`
- **Description:** After company signature, students upload the scanned convention document.

---

#### 3. Deliverable Management

**Use Case: Deposer les livrables (Deposit deliverables)**
- **API Endpoint:** `POST /api/deliverables`
- **Request DTO:** `DeliverableRequest`
  - Fields: `pfaId`, `fileTitle`, `fileUri`, `deliverableType`, `fileType`
- **Deliverable Types:** BEFORE_DEFENSE, AFTER_DEFENSE
- **File Types:** PROGRESS_REPORT, PRESENTATION, FINAL_REPORT
- **Description:** Students submit deliverables at different project phases.

**Use Case: Consulter les livrables (Consult deliverables)**
- **API Endpoints:**
  - `GET /api/deliverables/{id}` - Get deliverable details
  - `GET /api/deliverables/pfa/{pfaId}` - List student's deliverables
  - `GET /api/deliverables/pfa/{pfaId}/type/{type}` - List by type
- **Description:** Students verify submitted deliverables and track submission history.

---

#### 4. Soutenance Management

**Use Case: Consulter date de soutenance (Consult defense date)**
- **API Endpoint:** `GET /api/soutenances/pfa/{pfaId}`
- **Response DTO:** `SoutenanceResponse`
  - Fields: `soutenanceId`, `pfaId`, `studentName`, `pfaTitle`, `location`, `dateSoutenance`, `status`, `juryMembers`
- **Description:** Students query their defense date and location.

---

#### 5. Evaluation Management

**Use Case: Consulter les évaluations (Consult evaluations)**
- **API Endpoints:**
  - `GET /api/evaluations/{id}` - Get evaluation details
  - `GET /api/evaluations/pfa/{pfaId}` - List student's evaluations
- **Response DTO:** `EvaluationResponse`
  - Fields: `evaluationId`, `pfaId`, `pfaTitle`, `evaluatorId`, `evaluatorName`, `dateEvaluation`, `totalScore`
- **Description:** Students view evaluation scores and feedback from supervisors/evaluators.

---

#### REST Endpoints Summary (Student)

**PFADossierController**
```
POST   /api/pfa-dossiers/create-or-get      - Create or retrieve active dossier
GET    /api/pfa-dossiers/{id}                - Get dossier details
GET    /api/pfa-dossiers/student/{studentId} - List all student's dossiers
```

**ConventionController**
```
POST   /api/conventions                    - Request convention
POST   /api/conventions/{id}/upload-signed  - Upload signed document
GET    /api/conventions/{id}                - Get convention details
GET    /api/conventions/pfa/{pfaId}         - Get convention for PFA
```

**DeliverableController**
```
POST   /api/deliverables                    - Submit deliverable
GET    /api/deliverables/{id}                - Get deliverable details
GET    /api/deliverables/pfa/{pfaId}         - List student's deliverables
GET    /api/deliverables/pfa/{pfaId}/type/{type} - List by type
```

**SoutenanceController**
```
GET    /api/soutenances/pfa/{pfaId}         - Get defense date
```

**EvaluationController**
```
GET    /api/evaluations/{id}                - Get evaluation details
GET    /api/evaluations/pfa/{pfaId}         - List student's evaluations
```

---

### Supervisor/Encadrant Endpoints

#### SupervisorController - Student Management

```
GET    /api/supervisor/students?supervisorId={id}                  - Liste des étudiants avec leurs PFA
GET    /api/supervisor/students/{studentId}?supervisorId={id}      - Détail complet d'un étudiant
```

**Use Case: Consulter les étudiants encadrés**
- **Endpoint:** `GET /api/supervisor/students?supervisorId=10`
- **Method:** `SupervisorService.getMyStudents(Long supervisorId)`
- **Response DTO:** `List<StudentWithPFADTO>`
- **Description:** Retourne la liste de tous les étudiants encadrés avec leurs projets PFA actifs.

**Use Case: Voir le détail d'un étudiant**
- **Endpoint:** `GET /api/supervisor/students/50?supervisorId=10`
- **Method:** `SupervisorService.getStudentDetail(Long supervisorId, Long studentId)`
- **Response DTO:** `StudentDetailDTO`
- **Description:** Détail complet d'un étudiant incluant PFA, Convention, Livrables, et Soutenance.

---

#### SupervisorController - Deliverable Management

```
GET    /api/supervisor/deliverables?supervisorId={id}              - Tous les livrables des étudiants
GET    /api/supervisor/deliverables/pfa/{pfaId}                    - Livrables d'un PFA spécifique
```

**Use Case: Consulter tous les livrables**
- **Endpoint:** `GET /api/supervisor/deliverables?supervisorId=10`
- **Method:** `SupervisorService.getAllDeliverables(Long supervisorId)`
- **Response DTO:** `List<DeliverableDTO>`
- **Description:** Retourne tous les livrables soumis par les étudiants encadrés.

**Use Case: Consulter les livrables d'un PFA**
- **Endpoint:** `GET /api/supervisor/deliverables/pfa/20`
- **Method:** `SupervisorService.getDeliverablesByPfa(Long pfaId)`
- **Response DTO:** `List<DeliverableDTO>`
- **Description:** Retourne les livrables d'un projet PFA spécifique.

---

#### SupervisorController - Soutenance Management

```
GET    /api/supervisor/soutenances/pfas?supervisorId={id}          - Liste des PFAs avec leurs soutenances
GET    /api/supervisor/soutenances?supervisorId={id}               - Liste des soutenances uniquement
POST   /api/supervisor/soutenances?supervisorId={id}               - Planifier une nouvelle soutenance
PUT    /api/supervisor/soutenances/{id}?supervisorId={id}          - Modifier une soutenance
DELETE /api/supervisor/soutenances/{id}?supervisorId={id}          - Supprimer une soutenance
```

**Use Case: Consulter les PFAs avec soutenances**
- **Endpoint:** `GET /api/supervisor/soutenances/pfas?supervisorId=10`
- **Method:** `SoutenanceService.getPFAsWithSoutenances(Long supervisorId)`
- **Response DTO:** `List<PFAWithSoutenanceDTO>`
- **Description:** Liste des projets PFA avec leurs soutenances planifiées.

**Use Case: Consulter les soutenances**
- **Endpoint:** `GET /api/supervisor/soutenances?supervisorId=10`
- **Method:** `SoutenanceService.getSoutenancesBySupervisor(Long supervisorId)`
- **Response DTO:** `List<SoutenanceDTO>`
- **Description:** Liste uniquement les soutenances planifiées.

**Use Case: Planifier une soutenance**
- **Endpoint:** `POST /api/supervisor/soutenances?supervisorId=10`
- **Method:** `SoutenanceService.planifierSoutenance(Long supervisorId, SoutenanceRequest request)`
- **Request DTO:** `SoutenanceRequest`
  - Fields: `pfaId`, `location`, `dateSoutenance`, `juryMembers`
- **Response DTO:** `SoutenanceDTO`
- **Description:** Créer une nouvelle soutenance pour un projet PFA.

**Use Case: Modifier une soutenance**
- **Endpoint:** `PUT /api/supervisor/soutenances/15?supervisorId=10`
- **Method:** `SoutenanceService.modifierSoutenance(Long supervisorId, Long soutenanceId, SoutenanceRequest request)`
- **Request DTO:** `SoutenanceRequest`
- **Response DTO:** `SoutenanceDTO`
- **Description:** Modifier une soutenance existante (date, lieu, jury).

**Use Case: Supprimer une soutenance**
- **Endpoint:** `DELETE /api/supervisor/soutenances/15?supervisorId=10`
- **Method:** `SoutenanceService.supprimerSoutenance(Long supervisorId, Long soutenanceId)`
- **Response:** Success with null data
- **Description:** Supprimer une soutenance planifiée.

---

#### SupervisorController - Evaluation Management

```
GET    /api/supervisor/evaluation-criteria                         - Récupérer les critères d'évaluation actifs
GET    /api/supervisor/evaluations?supervisorId={id}               - Soutenances avec statut d'évaluation
POST   /api/supervisor/evaluations?supervisorId={id}               - Enregistrer une évaluation
```

**Use Case: Consulter les critères d'évaluation**
- **Endpoint:** `GET /api/supervisor/evaluation-criteria`
- **Method:** `EvaluationService.getActiveCriteria()`
- **Response DTO:** `List<EvaluationCriteriaDTO>`
- **Description:** Retourne les critères d'évaluation actifs définis par l'administration.

**Use Case: Consulter les soutenances à évaluer**
- **Endpoint:** `GET /api/supervisor/evaluations?supervisorId=10`
- **Method:** `EvaluationService.getSoutenancesWithEvaluations(Long supervisorId)`
- **Response DTO:** `List<SoutenanceWithEvaluationDTO>`
- **Description:** Liste des soutenances avec leur statut d'évaluation (évaluée ou non).

**Use Case: Enregistrer une évaluation**
- **Endpoint:** `POST /api/supervisor/evaluations?supervisorId=10`
- **Method:** `EvaluationService.saveEvaluation(Long supervisorId, EvaluationRequest request)`
- **Request DTO:** `EvaluationRequest`
  - Fields: `soutenanceId`, `evaluationDetails` (critère + note)
- **Response DTO:** `EvaluationDTO`
- **Description:** Soumettre une évaluation pour une soutenance avec notes par critère.

---

### Example Workflows

#### Student Workflow: Créer un dossier PFA et demander une convention

**STEP 1: Créer ou récupérer un dossier PFA**
```
POST /api/pfa-dossiers/create-or-get
{
  "studentId": 123,
  "title": "E-Commerce Platform",
  "description": "Internship project in web development",
  "supervisorId": null
}

Response:
{
  "pfaId": 456,
  "studentId": 123,
  "title": "E-Commerce Platform",
  "status": "CONVENTION_PENDING",
  "convention": null
}
```

**STEP 2: Demander une convention (Using pfaId from STEP 1)**
```
POST /api/conventions
{
  "studentId": 123,
  "pfaId": 456,
  "companyName": "TechCorp",
  "companyAddress": "123 Tech Street",
  "companySupervisorName": "KIHL Youns",
  "companySupervisorEmail": "kihlyouns@techcorp.com",
  "startDate": 1704067200000,
  "endDate": 1735689600000
}

Response:
{
  "conventionId": 789,
  "pfaId": 456,
  "state": "DEMAND_PENDING",
  "studentId": 123,
  "companyName": "TechCorp",
  "companySupervisorName": "KIHL Youns",
  "createdAt": 1704067200000
}
```

**STEP 3: Deposer la convention signée**
```
POST /api/conventions/789/upload-signed?scannedFileUri=/uploads/convention_signed.pdf

Response:
{
  "conventionId": 789,
  "state": "SIGNED_UPLOADED",
  "scannedFileUri": "/uploads/convention_signed.pdf"
}
```

---

#### Student Workflow: Deposer les livrables

**STEP 1: Deposer un livrable avant soutenance**
```
POST /api/deliverables
{
  "pfaId": 456,
  "fileTitle": "Rapport de progrès",
  "fileUri": "/uploads/progress_report.pdf",
  "deliverableType": "BEFORE_DEFENSE",
  "fileType": "PROGRESS_REPORT"
}

Response:
{
  "deliverableId": 101,
  "pfaId": 456,
  "fileTitle": "Rapport de progrès",
  "fileUri": "/uploads/progress_report.pdf",
  "deliverableType": "BEFORE_DEFENSE",
  "fileType": "PROGRESS_REPORT",
  "uploadedAt": 1704067200000,
  "validated": false
}
```

**STEP 2: Consulter tous les livrables**
```
GET /api/deliverables/pfa/456

Response:
[
  {
    "deliverableId": 101,
    "fileTitle": "Rapport de progrès",
    "deliverableType": "BEFORE_DEFENSE",
    "fileType": "PROGRESS_REPORT",
    "validated": false
  }
]
```

---

#### Supervisor Workflow: Planifier une soutenance

**STEP 1: Lister les PFAs**
```
GET /api/supervisor/soutenances/pfas?supervisorId=10

Response: List of PFAs with existing soutenances
```

**STEP 2: Planifier une nouvelle soutenance**
```
POST /api/supervisor/soutenances?supervisorId=10
{
  "pfaId": 20,
  "location": "Amphi A",
  "dateSoutenance": 1735689600000,
  "juryMembers": ["Dr. Ahmed", "Prof. Fatima"]
}

Response:
{
  "soutenanceId": 5,
  "pfaId": 20,
  "pfaTitle": "E-Commerce Platform",
  "studentName": "KIHL Youns",
  "location": "Amphi A",
  "dateSoutenance": 1735689600000,
  "status": "SCHEDULED"
}
```

---

#### Supervisor Workflow: Évaluer une soutenance

**STEP 1: Lister les PFAs**
```
GET /api/supervisor/soutenances/pfas?supervisorId=10

Response: List of PFAs with existing soutenances
```

**STEP 2: Planifier une nouvelle soutenance**
```
POST /api/supervisor/soutenances?supervisorId=10
{
  "pfaId": 20,
  "location": "Amphi A",
  "dateSoutenance": 1735689600000,
  "juryMembers": ["Dr. Ahmed", "Prof. Fatima"]
}

Response:
{
  "soutenanceId": 5,
  "pfaId": 20,
  "pfaTitle": "E-Commerce Platform",
  "studentName": "KIHL Youns",
  "location": "Amphi A",
  "dateSoutenance": 1735689600000,
  "status": "SCHEDULED"
}
```

---

#### Supervisor Workflow: Évaluer une soutenance

**STEP 1: Consulter les critères d'évaluation**
```
GET /api/supervisor/evaluation-criteria

Response:
[
  { "criteriaId": 1, "criteriaName": "Qualité technique", "maxScore": 20 },
  { "criteriaId": 2, "criteriaName": "Présentation orale", "maxScore": 10 },
  { "criteriaId": 3, "criteriaName": "Rapport écrit", "maxScore": 10 }
]
```

**STEP 2: Enregistrer l'évaluation**
```
POST /api/supervisor/evaluations?supervisorId=10
{
  "soutenanceId": 5,
  "evaluationDetails": [
    { "criteriaId": 1, "score": 18 },
    { "criteriaId": 2, "score": 9 },
    { "criteriaId": 3, "score": 8 }
  ]
}

Response:
{
  "evaluationId": 10,
  "pfaId": 20,
  "pfaTitle": "E-Commerce Platform",
  "evaluatorId": 10,
  "evaluatorName": "Prof. Alami",
  "dateEvaluation": 1735689600000,
  "totalScore": 35.0
}
```

---

### Coordinator/Coordinateur Endpoints

#### CoordinatorController - Student Management

```
GET    /api/coordinator/students/{departmentId}                    - Liste des étudiants avec évaluations
```

**Use Case: Consulter les étudiants avec évaluations**
- **Endpoint:** `GET /api/coordinator/students/1`
- **Method:** `CoordinatorService.getStudentsWithEvaluations(Long departmentId)`
- **Response DTO:** `List<StudentWithEvaluationDTO>`
  - Fields: `studentId`, `studentFirstName`, `studentLastName`, `studentEmail`, `pfaId`, `pfaTitle`, `pfaStatus`, `supervisorId`, `supervisorFirstName`, `supervisorLastName`, `evaluationId`, `totalScore`
- **Description:** Retourne la liste de tous les étudiants d'un département avec leurs informations PFA, superviseur et évaluation.

---

#### CoordinatorController - Professor Management

```
GET    /api/coordinator/professors/{departmentId}                  - Liste des professeurs avec statistiques
```

**Use Case: Consulter les professeurs avec statistiques**
- **Endpoint:** `GET /api/coordinator/professors/1`
- **Method:** `CoordinatorService.getProfessorsWithStats(Long departmentId)`
- **Response DTO:** `List<ProfessorWithStatsDTO>`
  - Fields: `professorId`, `professorFirstName`, `professorLastName`, `professorEmail`, `phoneNumber`, `studentCount`, `averageScore`
- **Description:** Retourne la liste des professeurs d'un département avec le nombre d'étudiants encadrés et la note moyenne.

---

#### CoordinatorController - Assignment Management

```
GET    /api/coordinator/assignments/{departmentId}                 - Affectations encadrants-étudiants
POST   /api/coordinator/assignments/auto-assign                    - Affectation automatique équitable
```

**Use Case: Consulter les affectations**
- **Endpoint:** `GET /api/coordinator/assignments/1`
- **Method:** `CoordinatorService.getAssignments(Long departmentId)`
- **Response DTO:** `List<ProfessorAssignmentDTO>`
  - Fields: `professorId`, `professorFirstName`, `professorLastName`, `professorEmail`, `students` (liste de `AssignedStudentDTO`)
- **Description:** Retourne les affectations encadrant-étudiants pour un département.

**Use Case: Affectation automatique équitable**
- **Endpoint:** `POST /api/coordinator/assignments/auto-assign`
- **Method:** `CoordinatorService.autoAssignStudentsToProfessors(Long departmentId)`
- **Request DTO:** `AutoAssignRequest`
  - Fields: `departmentId`
- **Response:** `Integer` (nombre d'étudiants affectés)
- **Description:** Affecte automatiquement les étudiants sans encadrant aux professeurs de manière équitable.

---

### Administration Endpoints

#### UserController - User Management

```
GET    /api/users                                                  - Liste tous les utilisateurs
GET    /api/users/{id}                                             - Récupérer un utilisateur par ID
POST   /api/users                                                  - Créer un nouvel utilisateur
PUT    /api/users/{id}                                             - Modifier un utilisateur
DELETE /api/users/{id}                                             - Supprimer un utilisateur
```

**Use Case: Lister les utilisateurs**
- **Endpoint:** `GET /api/users`
- **Response DTO:** `List<UserDto>`
  - Fields: `userId`, `email`, `password`, `firstName`, `lastName`, `role`, `phoneNumber`, `createdAt`, `departmentId`
- **Description:** Retourne la liste de tous les utilisateurs du système.

**Use Case: Récupérer un utilisateur**
- **Endpoint:** `GET /api/users/10`
- **Response DTO:** `User`
- **Description:** Retourne les détails d'un utilisateur spécifique.

**Use Case: Créer un utilisateur**
- **Endpoint:** `POST /api/users`
- **Request DTO:** `UserDto`
  - Fields: `email`, `password`, `firstName`, `lastName`, `role`, `phoneNumber`, `departmentId`
- **Response DTO:** `UserDto`
- **Description:** Crée un nouvel utilisateur dans le système.

**Use Case: Modifier un utilisateur**
- **Endpoint:** `PUT /api/users/10`
- **Request DTO:** `User`
- **Response DTO:** `User`
- **Description:** Met à jour les informations d'un utilisateur existant.

**Use Case: Supprimer un utilisateur**
- **Endpoint:** `DELETE /api/users/10`
- **Response:** `204 No Content`
- **Description:** Supprime un utilisateur du système.

---

#### DepartmentController - Department Management

```
GET    /api/departments                                            - Liste tous les départements
GET    /api/departments/{id}                                       - Récupérer un département par ID
POST   /api/departments                                            - Créer un nouveau département
PUT    /api/departments/{id}                                       - Modifier un département
DELETE /api/departments/{id}                                       - Supprimer un département
```

**Use Case: Lister les départements**
- **Endpoint:** `GET /api/departments`
- **Response DTO:** `List<DepartmentDto>`
  - Fields: `departmentId`, `name`, `code`
- **Description:** Retourne la liste de tous les départements.

**Use Case: Récupérer un département**
- **Endpoint:** `GET /api/departments/1`
- **Response DTO:** `Department`
- **Description:** Retourne les détails d'un département spécifique.

**Use Case: Créer un département**
- **Endpoint:** `POST /api/departments`
- **Request DTO:** `Department`
  - Fields: `name`, `code`
- **Response DTO:** `Department`
- **Description:** Crée un nouveau département.

**Use Case: Modifier un département**
- **Endpoint:** `PUT /api/departments/1`
- **Request DTO:** `Department`
- **Response DTO:** `Department`
- **Description:** Met à jour les informations d'un département existant.

**Use Case: Supprimer un département**
- **Endpoint:** `DELETE /api/departments/1`
- **Response:** `204 No Content`
- **Description:** Supprime un département du système.

---

#### REST Endpoints Summary (Coordinator)

**CoordinatorController**
```
GET    /api/coordinator/students/{departmentId}       - Students with evaluations
GET    /api/coordinator/professors/{departmentId}     - Professors with stats
GET    /api/coordinator/assignments/{departmentId}    - Professor-student assignments
POST   /api/coordinator/assignments/auto-assign       - Auto-assign students
```

#### REST Endpoints Summary (Administration)

**UserController**
```
GET    /api/users                                     - List all users
GET    /api/users/{id}                                - Get user by ID
POST   /api/users                                     - Create user
PUT    /api/users/{id}                                - Update user
DELETE /api/users/{id}                                - Delete user
```

**DepartmentController**
```
GET    /api/departments                               - List all departments
GET    /api/departments/{id}                          - Get department by ID
POST   /api/departments                               - Create department
PUT    /api/departments/{id}                          - Update department
DELETE /api/departments/{id}                          - Delete department
```

---

### Coordinator Workflow: Affectation automatique des encadrants

**STEP 1: Consulter les professeurs disponibles**
```
GET /api/coordinator/professors/1

Response:
[
  {
    "professorId": 10,
    "professorFirstName": "Ahmed",
    "professorLastName": "Alami",
    "professorEmail": "ahmed.alami@ensate.ma",
    "phoneNumber": "0612345678",
    "studentCount": 3,
    "averageScore": 15.5
  },
  {
    "professorId": 11,
    "professorFirstName": "Fatima",
    "professorLastName": "Benali",
    "professorEmail": "fatima.benali@ensate.ma",
    "phoneNumber": "0623456789",
    "studentCount": 2,
    "averageScore": 16.0
  }
]
```

**STEP 2: Lancer l'affectation automatique**
```
POST /api/coordinator/assignments/auto-assign
{
  "departmentId": 1
}

Response:
{
  "success": true,
  "message": "5 étudiant(s) affecté(s) avec succès",
  "data": 5
}
```

**STEP 3: Vérifier les affectations**
```
GET /api/coordinator/assignments/1

Response:
[
  {
    "professorId": 10,
    "professorFirstName": "Ahmed",
    "professorLastName": "Alami",
    "professorEmail": "ahmed.alami@ensate.ma",
    "students": [
      {
        "studentId": 50,
        "studentFirstName": "Youns",
        "studentLastName": "KIHL",
        "studentEmail": "youns.kihl@student.ensate.ma",
        "pfaId": 20,
        "pfaTitle": "E-Commerce Platform"
      }
    ]
  }
]
```

---

### Coordinator Workflow: Superviser les évaluations

**STEP 1: Consulter les étudiants avec évaluations**
```
GET /api/coordinator/students/1

Response:
[
  {
    "studentId": 50,
    "studentFirstName": "Youns",
    "studentLastName": "KIHL",
    "studentEmail": "youns.kihl@student.ensate.ma",
    "pfaId": 20,
    "pfaTitle": "E-Commerce Platform",
    "pfaStatus": "IN_PROGRESS",
    "supervisorId": 10,
    "supervisorFirstName": "Ahmed",
    "supervisorLastName": "Alami",
    "evaluationId": 5,
    "totalScore": 35.0
  }
]
```

---

## Validation & Error Handling

### Exception Handling
- **GlobalExceptionHandler** - Centralized error response formatting
- **ResourceNotFoundException** - For missing entities (404)
- **BadRequestException** - For business logic violations (400)
- **ErrorResponse** - Standardized error response DTO

### Common Error Responses

**Resource Not Found (404)**
```json
{
  "success": false,
  "message": "PFA dossier with id 456 not found",
  "data": null
}
```

**Bad Request (400)**
```json
{
  "success": false,
  "message": "Convention already exists for this PFA",
  "data": null
}
```

**Success Response**
```json
{
  "success": true,
  "message": null,
  "data": { /* DTO object */ }
}
```

---
