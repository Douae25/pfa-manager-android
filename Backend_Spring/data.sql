-- =====================================================
-- PFA Management System - Data Script
-- =====================================================


-- =====================================================
-- 1. DEPARTMENTS
-- =====================================================

INSERT INTO departments (code, name) VALUES
('GI', 'Génie Informatique'),
('BDIA', 'Big Data et Intelligence Artificielle'),
('SCM', 'Supply Chain Management'),
('GM', 'Génie Mécatronique'),
('GC', 'Génie Civil'),
('GSTR', 'Génie des Systèmes et Télécommunications')
ON DUPLICATE KEY UPDATE code = code;

-- =====================================================
-- 2. USERS 
-- =====================================================
INSERT INTO users (user_id, email, password, first_name, last_name, role, department_id, phone_number, created_at) VALUES
-- Nada Elmourabet 
(1, 'nada.elmourabet@etu.uae.ac.ma', 'password', 'Nada', 'Elmourabet', 'COORDINATOR', 1, '+212600000001', NOW()),
-- Lina Aitbrahim 
(2, 'lina.aitbrahim@etu.uae.ac.ma', 'password', 'Lina', 'Aitbrahim', 'ADMIN', null, '+212600000002', NOW()),
-- Douae Aazibou 
(3, 'douae.aazibou@etu.uae.ac.ma', 'password', 'Douae', 'Aazibou', 'STUDENT', 1, '+212600000003', NOW()),
-- Youness Elkihal 
(4, 'youness.elkihal@etu.uae.ac.ma', 'password', 'Youness', 'Elkihal', 'STUDENT', 1, '+212600000004', NOW()),
-- Saad Barhrouj 
(5, 'saad.barhrouj@etu.uae.ac.ma', 'password', 'Saad', 'Barhrouj', 'PROFESSOR', 1, '+212600000005', NOW())
ON DUPLICATE KEY UPDATE user_id = user_id;

