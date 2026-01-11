package ma.ensate.pfa_manager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.*;
import ma.ensate.pfa_manager.model.*;

public class StudentDetailRepository {

    private final UserDao userDao;
    private final PFADossierDao pfaDao;
    private final DeliverableDao deliverableDao;
    private final SoutenanceDao soutenanceDao;
    private final ConventionDao conventionDao;

    public StudentDetailRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        pfaDao = db.pfaDossierDao();
        deliverableDao = db.deliverableDao();
        soutenanceDao = db.soutenanceDao();
        conventionDao = db.conventionDao();
    }

    // Récupérer l'étudiant
    public LiveData<User> getStudent(Long studentId) {
        return userDao.getUserById(studentId);
    }

    // Récupérer le PFA de l'étudiant
    public LiveData<List<PFADossier>> getStudentPFAs(Long studentId) {
        return pfaDao.getPFAsByStudent(studentId);
    }

    // Récupérer les livrables du PFA
    public LiveData<List<Deliverable>> getPFADeliverables(Long pfaId) {
        return deliverableDao.getDeliverablesByPFA(pfaId);
    }

    // Récupérer la soutenance du PFA
    public LiveData<Soutenance> getPFASoutenance(Long pfaId) {
        return soutenanceDao.getSoutenanceByPFA(pfaId);
    }

    // Récupérer la convention du PFA
    public LiveData<Convention> getPFAConvention(Long pfaId) {
        return conventionDao.getConventionByPFA(pfaId);
    }

    // Compter les livrables
    public LiveData<Integer> countDeliverables(Long pfaId) {
        return deliverableDao.countDeliverablesByPFA(pfaId);
    }
}