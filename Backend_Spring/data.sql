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

-- Update existing users with complete information
UPDATE users SET first_name = 'Admin', last_name = 'User', phone_number = '+212600000001', department_id = NULL WHERE user_id = 1;
UPDATE users SET first_name = 'Lina', last_name = 'Aitbrahim', phone_number = '+212600000002', department_id = NULL WHERE user_id = 2;
UPDATE users SET first_name = 'Douae', last_name = 'Aazibou', phone_number = '+212600000003', department_id = 1 WHERE user_id = 3;
UPDATE users SET first_name = 'Youness', last_name = 'Elkihal', phone_number = '+212600000004', department_id = 1 WHERE user_id = 4;
UPDATE users SET first_name = 'Saad', last_name = 'Barhrouj', phone_number = '+212600000005', department_id = 1 WHERE user_id = 5;

-- Insert new users if they don't exist
INSERT INTO users (user_id, email, password, first_name, last_name, role, department_id, phone_number, created_at) VALUES
-- Nada Elmourabet 
(6, 'nada.elmourabet@etu.uae.ac.ma', 'password', 'Nada', 'Elmourabet', 'COORDINATOR', 1, '+212600000006', UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE user_id = user_id;

