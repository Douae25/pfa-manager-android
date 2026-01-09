package ma.ensate.pfa_manager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.ConventionDao;
import ma.ensate.pfa_manager.database.DeliverableDao;
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

    public DashboardRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        pfaDao = db.pfaDossierDao();
        deliverableDao = db.deliverableDao();
        soutenanceDao = db.soutenanceDao();
        conventionDao = db.conventionDao();
    }

    // Récupérer l'utilisateur connecté
    public LiveData<User> getCurrentUser(Long userId) {
        return userDao.getUserById(userId);
    }

    // Compter les étudiants affectés
    public LiveData<List<User>> getMyStudents(Long supervisorId) {
        return userDao.getStudentsBySupervisor(supervisorId);
    }

    // Compter les PFAs supervisés
    public LiveData<Integer> getPFACount(Long supervisorId) {
        return pfaDao.countPFAsBySupervisor(supervisorId);
    }

    // Compter les livrables à valider
    public LiveData<Integer> getDeliverablesCount(Long supervisorId) {
        return deliverableDao.countDeliverablesBySupervisor(supervisorId);
    }

    // Compter les soutenances planifiées
    public LiveData<Integer> getSoutenancesCount(Long supervisorId) {
        return soutenanceDao.countPlannedSoutenancesBySupervisor(supervisorId);
    }

    // Compter les conventions à valider
    public LiveData<Integer> getConventionsToValidateCount(Long supervisorId) {
        return conventionDao.countConventionsToValidate(supervisorId);
    }

    // Prochaines soutenances (pour notifications)
    public LiveData<List<Soutenance>> getUpcomingSoutenances(Long supervisorId) {
        return soutenanceDao.getUpcomingSoutenances(supervisorId);
    }
}