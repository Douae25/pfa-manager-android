# Backend_Spring - Student Documentation


## Student Use Cases Mapping

### 1. PFA Dossier Management

#### Use Case: Créer ou récupérer un dossier PFA (Create or retrieve PFA dossier)
- **API Endpoint:** `POST /api/pfa-dossiers/create-or-get`
- **Method:** `PFADossierService.createOrGetDossier(PFADossierRequest request)`
- **Implementation Class:** `PFADossierServiceImpl`
- **Controller:** `PFADossierController`
- **Request DTO:** `PFADossierRequest`
  - Fields: `studentId`, `title`, `description`, `supervisorId` (optional)
- **Response DTO:** `PFADossierResponse`
- **Justification:** This is STEP 1 before requesting a convention. Students create a new PFA dossier or reuse an existing active one. The backend handles:
  - Finding existing ACTIVE dossiers (CONVENTION_PENDING)
  - Creating new dossier if none active exists

#### Use Case: Consulter les dossiers PFA (Consult PFA dossiers)
- **API Endpoints:**
  - `GET /api/pfa-dossiers/{id}` → `getDossierById(Long id)` - Get specific dossier
  - `GET /api/pfa-dossiers/student/{studentId}` → `getDossiersByStudent(Long studentId)` - List all student's dossiers
- **Implementation:** `PFADossierServiceImpl`
- **Justification:** Students query their PFA dossiers to view project status, linked convention (if exists), and metadata. Returns single convention object per dossier (ONE-TO-ONE relationship).

**Relationship Model:**
```
Student (1) ──→ (0..N) PFADossier
                        ├─ title
                        ├─ description
                        ├─ currentStatus: CONVENTION_PENDING
                        └─ convention (0..1) ← ONE convention per dossier
```

### 2. Convention Management

#### Use Case: Demander convention de stage (Request internship convention)
- **API Endpoint:** `POST /api/conventions`
- **Method:** `ConventionService.requestConvention(ConventionRequest request)`
- **Implementation Class:** `ConventionServiceImpl`
- **Controller:** `ConventionController`
- **Request DTO:** `ConventionRequest`
  - Fields: `studentId`, `pfaId`, `companyName`, `companyAddress`, `companySupervisorName`, `companySupervisorEmail`, `startDate`, `endDate`
- **Response DTO:** `ConventionResponse`
- **Workflow:** 
  1. Takes `pfaId` from STEP 1 response
  2. Validates student ownership (security check)
  3. Prevents duplicate conventions (throws error if convention exists)
  4. Creates convention with state DEMAND_PENDING
- **Justification:** STEP 2 after dossier creation. Students request a convention for their internship placement, initiating the convention workflow with company and institution.

#### Use Case: Consulter la convention (Consult convention)
- **API Endpoints:**
  - `GET /api/conventions/{id}` → `getConventionById(Long id)`
  - `GET /api/conventions/pfa/{pfaId}` → `getConventionByPfaId(Long pfaId)`
- **Implementation:** `ConventionServiceImpl`
- **Justification:** Students view their convention details, including company information, dates, and signed document URI. Returns null if no convention yet.

#### Use Case: Deposer convention signee (Upload signed convention)
- **API Endpoint:** `POST /api/conventions/{id}/upload-signed?scannedFileUri=<uri>`
- **Method:** `ConventionService.uploadSignedConvention(Long conventionId, String scannedFileUri)`
- **Implementation:** `ConventionServiceImpl`
- **Justification:** After company signature, students upload the scanned convention document. This updates the convention's state to SIGNED_UPLOADED.
- **State Validation:** Convention must be in DEMAND_APPROVED state before upload is allowed.

---

### 2. Deliverable Management

#### Use Case: Deposer les livrables (Deposit deliverables)
- **API Endpoint:** `POST /api/deliverables`
- **Method:** `DeliverableService.depositDeliverable(DeliverableRequest request)`
- **Implementation:** `DeliverableServiceImpl`
- **Request DTO:** `DeliverableRequest`
  - Fields: `pfaId`, `fileTitle`, `fileUri`, `deliverableType`, `fileType`
- **Deliverable Types:** BEFORE_DEFENSE, AFTER_DEFENSE
- **File Types:** PROGRESS_REPORT, PRESENTATION, FINAL_REPORT
- **Justification:** Students submit deliverables at different project phases. The system tracks upload time and validation status. File type enums (DeliverableFileType) distinguish report categories for administrative clarity.

#### Use Case: Consult deliverables (Related to Deposer les livrables)
- **API Endpoints:**
  - `GET /api/deliverables/{id}` → Query individual deliverable
  - `GET /api/deliverables/pfa/{pfaId}` → Query all deliverables for student's PFA
  - `GET /api/deliverables/pfa/{pfaId}/type/{type}` → Query deliverables by specialization type
- **Implementation:** `DeliverableServiceImpl`
- **Justification:** Students need to verify submitted deliverables and track their submission history.


---

### 3. Soutenance (Defense) Management

#### Use Case: Consulter date de soutenance (Consult defense date)
- **API Endpoint:** `GET /api/soutenances/pfa/{pfaId}`
- **Method:** `SoutenanceService.getSoutenanceByPfaId(Long pfaId)`
- **Implementation:** `SoutenanceServiceImpl`
- **Response DTO:** `SoutenanceResponse`
  - Fields: `soutenanceId`, `pfaId`, `studentName`, `pfaTitle`, `location`, `dateSoutenance`, `status`, `createdAt`, `juryMembers`
- **Justification:** Students must query their defense date and location. The endpoint returns only the soutenance for their PFA, preventing unauthorized access to other students' defense information.

---

### 4. Evaluation Management

#### Use Case: Consulter les évaluations (Consult evaluations)
- **API Endpoints:**
  - `GET /api/evaluations/{id}` → Query individual evaluation
  - `GET /api/evaluations/pfa/{pfaId}` → Query all evaluations for student's PFA
- **Implementation:** `EvaluationServiceImpl`
- **Response DTO:** `EvaluationResponse`
  - Fields: `evaluationId`, `pfaId`, `pfaTitle`, `evaluatorId`, `evaluatorName`, `dateEvaluation`, `totalScore`
- **Justification:** Students view evaluation scores and feedback from supervisors/evaluators. Total score provides quick performance summary. Evaluator name contextualizes feedback source.

---

### 5. PFA Dossier Management

#### Use Case: Associated with convention and deliverable consultation
- **API Endpoints:**
  - `GET /api/pfa-dossiers/{id}` → View dossier details
  - `GET /api/pfa-dossiers/student/{studentId}` → List all student's dossiers
- **Implementation:** `PFADossierServiceImpl`
- **Justification:** Students query their PFA dossier to verify project status, linked convention, and metadata. The getDossiersByStudent() endpoint enables students to see all their projects.


---

## Controller Layer Architecture

### REST Endpoints (Student)

#### PFADossierController
```
POST   /api/pfa-dossiers/create-or-get      - Create or retrieve active dossier
GET    /api/pfa-dossiers/{id}                - Get dossier details
GET    /api/pfa-dossiers/student/{studentId} - List all student's dossiers
```

#### ConventionController
```
POST   /api/conventions                    - Request convention
POST   /api/conventions/{id}/upload-signed  - Upload signed document
GET    /api/conventions/{id}                - Get convention details
GET    /api/conventions/pfa/{pfaId}         - Get convention for PFA
```

#### DeliverableController
```
POST   /api/deliverables                    - Submit deliverable
GET    /api/deliverables/{id}                - Get deliverable details
GET    /api/deliverables/pfa/{pfaId}         - List student's deliverables
GET    /api/deliverables/pfa/{pfaId}/type/{type} - List by type
```

#### SoutenanceController
```
GET    /api/soutenances/pfa/{pfaId}         - Get defense date
```

#### EvaluationController
```
GET    /api/evaluations/{id}                - Get evaluation details
GET    /api/evaluations/pfa/{pfaId}         - List student's evaluations
```


---

## Validation & Error Handling

### Preserved Exception Handling
- **GlobalExceptionHandler** - Centralized error response formatting
- **ResourceNotFoundException** - For missing entities
- **BadRequestException** - For business logic violations
- **ErrorResponse** - Standardized error response DTO

---

## Appendix: Two-Step Workflow

### STEP 1: Create or Retrieve PFA Dossier
```
POST /api/pfa-dossiers/create-or-get
{
  "studentId": 123,
  "title": "E-Commerce Platform",
  "description": "Internship project in web development",
  "supervisorId": null  // Optional
}

Response:
{
  "pfaId": 456,
  "studentId": 123,
  "title": "E-Commerce Platform",
  "status": "CONVENTION_PENDING",
  "convention": null  // Will be populated after STEP 2
}
```

**Student receives `pfaId` = 456 for use in STEP 2**

### STEP 2: Request Convention (Using pfaId from STEP 1)
```
POST /api/conventions
{
  "studentId": 123,      // For security verification
  "pfaId": 456,          // From STEP 1 response
  "companyName": "TechCorp",
  "companyAddress": "123 Tech Street",
  "companySupervisorName": "KIHL Youns",
  "companySupervisorEmail": "kihlyouns@techcorp.com",
  "startDate": 1704067200000,  // Jan 1, 2024
  "endDate": 1735689600000     // Jan 1, 2025
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

---

**Document Generated:** January 11, 2025  
**Project:** PFA Management System - Backend_Spring (Student-Only Extract)  
**Status:** ✓ Complete - Ready for Integration Testing
