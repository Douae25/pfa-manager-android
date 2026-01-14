package ma.ensate.pfa_manager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.ConventionDao;
import ma.ensate.pfa_manager.database.DeliverableDao;
import ma.ensate.pfa_manager.database.EvaluationDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.SoutenanceDao;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.*;

public class DashboardRepository {

    private final UserDao userDao;
    private final PFADossierDao pfaDao;
    private final DeliverableDao deliverableDao;
    private final SoutenanceDao soutenanceDao;
    private final ConventionDao conventionDao;
    private final EvaluationDao evaluationDao;

    public DashboardRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        pfaDao = db.pfaDossierDao();
        deliverableDao = db.deliverableDao();
        soutenanceDao = db.soutenanceDao();
        conventionDao = db.conventionDao();
        evaluationDao = db.evaluationDao();
    }

    // Récupérer l'utilisateur connecté
    public LiveData<User> getCurrentUser(Long userId) {
        return userDao.getUserById(userId);
    }

    // Récupérer les étudiants affectés
    public LiveData<List<User>> getMyStudents(Long supervisorId) {
        return userDao.getStudentsBySupervisor(supervisorId);
    }

    // Compter les livrables à valider
    public LiveData<Integer> getDeliverablesCount(Long supervisorId) {
        return deliverableDao.countDeliverablesBySupervisor(supervisorId);
    }

    // Compter les soutenances planifiées
    public LiveData<Integer> getSoutenancesCount(Long supervisorId) {
        return soutenanceDao.countPlannedSoutenancesBySupervisor(supervisorId);
    }

    // Compter les soutenances NON planifiées (PFA sans soutenance)
    public LiveData<Integer> getUnplannedSoutenancesCount(Long supervisorId) {
        return soutenanceDao.countUnplannedSoutenancesBySupervisor(supervisorId);
    }

    // Compter les étudiants NON évalués (PFA sans évaluation)
    public LiveData<Integer> getUnevaluatedStudentsCount(Long supervisorId) {
        return evaluationDao.countUnevaluatedStudentsBySupervisor(supervisorId);
    }
}