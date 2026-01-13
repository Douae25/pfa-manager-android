package ma.ensate.pfa_manager.database;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.model.Department;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;

public class DatabaseInitializer {

    private static final String[] COORDINATOR_EMAILS = {
            "coordinator.gi@ensate.ma",
            "coordinator.gm@ensate.ma",
            "coordinator.gc@ensate.ma",
            "coordinator.scm@ensate.ma",
            "coordinator.bda@ensate.ma"
    };

    private static final String[] COORDINATOR_PASSWORDS = {
            "coordgi123",
            "coordgm123",
            "coordgc123",
            "coordscm123",
            "coordbda123"
    };

    private static final String[] COORDINATOR_FIRST_NAMES = {
            "Coordinateur GI",
            "Coordinateur GM",
            "Coordinateur GC",
            "Coordinateur SCM",
            "Coordinateur BDA"
    };

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_DB_INITIALIZED = "db_initialized";

    public static void initializeWithDefaultData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Vérifier si déjà initialisé
        if (prefs.getBoolean(KEY_DB_INITIALIZED, false)) {
            return; // Déjà initialisé, ne rien faire
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                UserDao userDao = db.userDao();
                DepartmentDao departmentDao = db.departmentDao();

                // 1. Créer les départements s'ils n'existent pas
                List<Department> existingDepartments = departmentDao.getAll();
                if (existingDepartments == null || existingDepartments.isEmpty()) {
                    createDepartments(departmentDao);
                    existingDepartments = departmentDao.getAll();
                }

                // 2. Créer un coordinateur pour chaque département
                for (int i = 0; i < existingDepartments.size() && i < COORDINATOR_EMAILS.length; i++) {
                    Department dept = existingDepartments.get(i);
                    String email = COORDINATOR_EMAILS[i];

                    // Vérifier si le coordinateur existe déjà
                    User existingCoordinator = userDao.getUserByEmailSync(email);

                    if (existingCoordinator == null) {
                        User coordinator = new User();
                        coordinator.setEmail(email);
                        coordinator.setPassword(COORDINATOR_PASSWORDS[i]);
                        coordinator.setFirst_name(COORDINATOR_FIRST_NAMES[i]);
                        coordinator.setLast_name("Filière");
                        coordinator.setRole(Role.COORDINATOR);
                        coordinator.setPhone_number("+21260000" + String.format("%04d", i+1));
                        coordinator.setDepartment_id(dept.getDepartment_id());
                        coordinator.setCreated_at(System.currentTimeMillis());

                        userDao.insert(coordinator);

                        // 3. Créer des étudiants et professeurs pour ce département
                        createTestUsersForDepartment(userDao, dept.getDepartment_id(), dept.getName(), i);
                    }
                }

                // Marquer comme initialisé
                prefs.edit().putBoolean(KEY_DB_INITIALIZED, true).apply();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void createDepartments(DepartmentDao departmentDao) {
        String[] departmentNames = {
                "Génie Informatique",
                "Génie Mécatronique",
                "Génie Civil",
                "Supply Chain Management",
                "Big Data & IA"
        };

        String[] departmentCodes = {"ADM", "GI2", "GM2", "GC2", "SCM2", "BDA2"};

        for (int i = 0; i < departmentNames.length; i++) {
            Department dept = new Department();
            dept.setName(departmentNames[i]);
            dept.setCode(departmentCodes[i]);
            departmentDao.insert(dept);
        }
    }

    private static void createTestUsersForDepartment(UserDao userDao, Long departmentId, String departmentName, int deptIndex) {
        String[] studentFirstNames = {"Karim", "Fatima", "Mohamed", "Amina", "Hassan", "Nadia"};
        String[] studentLastNames = {"Benali", "Zahra", "Alaoui", "El Mansouri", "Chakir", "Idrissi"};

        String[] professorFirstNames = {"Professeur", "Encadrant", "Superviseur"};
        String[] professorLastNames = {"Principal", "Senior", "Junior"};

        // Simplifier le nom du département pour l'email
        String deptCode = departmentName.substring(0, 2).toUpperCase();
        String deptEmail = deptCode.toLowerCase();

        // Créer des étudiants pour ce département
        for (int i = 0; i < 4; i++) {
            User student = new User();
            student.setEmail("etudiant" + (i+1) + "." + deptEmail + "@ensate.ma");
            student.setPassword("student123");
            student.setFirst_name(studentFirstNames[i % studentFirstNames.length]);
            student.setLast_name(studentLastNames[i % studentLastNames.length]);
            student.setRole(Role.STUDENT);
            student.setPhone_number("+2126" + String.format("%07d", deptIndex * 100 + i));
            student.setDepartment_id(departmentId);
            student.setCreated_at(System.currentTimeMillis());

            userDao.insert(student);
        }

        // Créer des professeurs pour ce département
        for (int i = 0; i < 2; i++) {
            User professor = new User();
            professor.setEmail("prof" + (i+1) + "." + deptEmail + "@ensate.ma");
            professor.setPassword("prof123");
            professor.setFirst_name(professorFirstNames[i]);
            professor.setLast_name(professorLastNames[i]);
            professor.setRole(Role.PROFESSOR);
            professor.setPhone_number("+2125" + String.format("%07d", deptIndex * 100 + i));
            professor.setDepartment_id(departmentId);
            professor.setCreated_at(System.currentTimeMillis());

            userDao.insert(professor);
        }
    }

    // Méthode pour réinitialiser (pour tests)
    public static void resetInitialization(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DB_INITIALIZED, false).apply();
    }
}