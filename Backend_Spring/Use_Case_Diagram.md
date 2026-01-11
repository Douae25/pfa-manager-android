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

* **Affecter les encardants aux etudiants** (Assign supervisors to students) *(Note: "encardants" is written as such in the diagram)*
* **Plannifier les soutenances** (Plan defenses)
* *Extension:* **Affecter les jurry** (Assign jury) *(Note: "jurry" is written as such in the diagram)*


* **Superviser les évaluation** (Supervise evaluations)

### 4. Encadrant (Supervisor)

* **Consulter la liste des etudiants a encadrer** (Consult list of students to supervise)
* **Valider les livrables** (Validate deliverables)
* **Consulter les notifications** (Consult notifications)
* **Evaluer un PFA** (Evaluate a PFA)

### 5. Shared / System Utility Use Cases

These are use cases that are included or extended by others but not directly triggered by an actor in the main flow:

* **S'authentifier** (Authenticate) – *Included by almost all main use cases.*
* **Envoyer des notifications** (Send notifications) – *Included by "Demander convention de stage" and "Recevoir les notifications".*
* **Calculer la note finale** (Calculate final grade) – *Included by "Evaluer un PFA" and "Superviser les évaluation".*
