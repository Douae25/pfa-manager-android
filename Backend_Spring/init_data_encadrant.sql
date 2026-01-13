-- AJUSTEMENT DES COLONNES
ALTER TABLE conventions MODIFY COLUMN state VARCHAR(50);
ALTER TABLE deliverables MODIFY COLUMN file_type VARCHAR(50);

-- NETTOYAGE COMPLET
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE soutenance_jury;
TRUNCATE TABLE evaluation_details;
TRUNCATE TABLE evaluations;
TRUNCATE TABLE evaluation_criteria;
TRUNCATE TABLE soutenances;
TRUNCATE TABLE deliverables;
TRUNCATE TABLE conventions;
TRUNCATE TABLE pfa_dossiers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 1. UTILISATEURS (Mansour ID=1, Saad ID=3, Nada ID=4)
-- ==========================================
INSERT INTO users (user_id, email, password, first_name, last_name, role, phone_number, created_at) VALUES 
(1, 'mansour@ensate.uae.ma', '$2a$10$EncryptedPasswordHere', 'Abdeljebar', 'Mansour', 'PROFESSOR', '0612345678', UNIX_TIMESTAMP()*1000),
(3, 'saad.barhrouj@etu.uae.ma', '$2a$10$EncryptedPasswordHere', 'Saad', 'Barhrouj', 'STUDENT', '0611111111', UNIX_TIMESTAMP()*1000),
(4, 'nada.elmourabet@etu.uae.ma', '$2a$10$EncryptedPasswordHere', 'Nada', 'El Mourabet', 'STUDENT', '0622222222', UNIX_TIMESTAMP()*1000);

-- ==========================================
-- 2. DOSSIERS PFA (Liés aux étudiants 3/4 et Supervisor 1)
-- ==========================================
-- PFA ID 1 pour Saad (ID 3), Supervisor (ID 1)
INSERT INTO pfa_dossiers (pfa_id, title, description, current_status, student_id, supervisor_id, updated_at) VALUES 
(1, 'Gestion PFA Mobile', 'Application Android Room/Spring Boot', 'IN_PROGRESS', 3, 1, UNIX_TIMESTAMP()*1000);

-- PFA ID 2 pour Nada (ID 4), Supervisor (ID 1)
INSERT INTO pfa_dossiers (pfa_id, title, description, current_status, student_id, supervisor_id, updated_at) VALUES 
(2, 'Système IoT Agricole', 'Capteurs intelligents pour irrigation', 'CONVENTION_PENDING', 4, 1, UNIX_TIMESTAMP()*1000);

-- ==========================================
-- 3. CONVENTIONS (Liées aux PFA 1 et 2) - IP MISE À JOUR ICI
-- ==========================================
-- Convention liée au PFA 1 (Saad)
INSERT INTO conventions (pfa_id, company_name, company_address, company_supervisor_name, company_supervisor_email, start_date, end_date, is_validated, state, scanned_file_uri) VALUES 
(1, 'Capgemini', 'Casanearshore', 'M. Responsable', 'rh@capgemini.com', UNIX_TIMESTAMP()*1000, (UNIX_TIMESTAMP() + 7776000)*1000, 1, 'VALIDATED', 'http://10.119.71.25:8080/uploads/convention_saad.pdf');

-- Convention liée au PFA 2 (Nada)
INSERT INTO conventions (pfa_id, company_name, company_address, company_supervisor_name, company_supervisor_email, start_date, end_date, is_validated, state, scanned_file_uri) VALUES 
(2, 'DXC Technology', 'Technopolis Rabat', 'Mme. Directrice', 'rh@dxc.com', UNIX_TIMESTAMP()*1000, (UNIX_TIMESTAMP() + 7776000)*1000, 0, 'SIGNED_UPLOADED', 'http://10.119.71.25:8080/uploads/convention_nada.pdf');

-- ==========================================
-- 4. LIVRABLES (Liés aux PFA 1 et 2) - IP DÉJÀ À JOUR
-- ==========================================
INSERT INTO deliverables (pfa_id, file_title, file_uri, deliverable_type, file_type, uploaded_at, is_validated) VALUES 
-- Saad (PFA 1) : Rapport
(1, 'Rapport V1', 'http://10.119.71.25:8080/uploads/rapport.pdf', 'BEFORE_DEFENSE', 'PROGRESS_REPORT', UNIX_TIMESTAMP()*1000, 1),
-- Saad (PFA 1) : Présentation
(1, 'Présentation', 'http://10.119.71.25:8080/uploads/pres.pdf', 'AFTER_DEFENSE', 'PRESENTATION', UNIX_TIMESTAMP()*1000, 0),
-- Nada (PFA 2) : Cahier des charges
(2, 'Cahier des charges', 'http://10.119.71.25:8080/uploads/rapport.pdf', 'BEFORE_DEFENSE', 'PROGRESS_REPORT', UNIX_TIMESTAMP()*1000, 0);

-- ==========================================
-- 5. SOUTENANCE (Commence à l'ID 1)
-- ==========================================
-- Soutenance ID 1 pour PFA 1 (Saad)
INSERT INTO soutenances (soutenance_id, pfa_id, location, date_soutenance, status, created_at) VALUES 
(1, 1, 'Salle B2', (UNIX_TIMESTAMP() + 604800)*1000, 'PLANNED', UNIX_TIMESTAMP()*1000);

-- ==========================================
-- 6. JURY (Lié à la Soutenance 1 et Mansour 1)
-- ==========================================
INSERT INTO soutenance_jury (soutenance_id, user_id) VALUES (1, 1);

-- ==========================================
-- 7. CRITÈRES (IDs 1, 2, 3)
-- ==========================================
INSERT INTO evaluation_criteria (criteria_id, label, max_points, description, is_active) VALUES 
(1, 'Qualité Technique', 10, 'Code, Architecture, Outils', 1),
(2, 'Qualité Rapport', 5, 'Rédaction, Schémas', 1),
(3, 'Présentation Orale', 5, 'Eloquence, Support visuel', 1);