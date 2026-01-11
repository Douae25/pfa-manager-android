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
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.Evaluation;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.User;

public class StudentDetailRepository {

    private final UserDao userDao;
    private final PFADossierDao pfaDao;
    private final DeliverableDao deliverableDao;
    private final SoutenanceDao soutenanceDao;
    private final ConventionDao conventionDao;
    private final EvaluationDao evaluationDao;

    public StudentDetailRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        pfaDao = db.pfaDossierDao();
        deliverableDao = db.deliverableDao();
        soutenanceDao = db.soutenanceDao();
        conventionDao = db.conventionDao();
        evaluationDao = db.evaluationDao();
    }

    public LiveData<User> getStudent(Long studentId) {
        return userDao.getUserById(studentId);
    }

    public LiveData<List<PFADossier>> getStudentPFAs(Long studentId) {
        return pfaDao.getPFAsByStudent(studentId);
    }

    public LiveData<List<Deliverable>> getPFADeliverables(Long pfaId) {
        return deliverableDao.getByPfaId(pfaId);
    }

    public LiveData<Soutenance> getPFASoutenance(Long pfaId) {
        return soutenanceDao.getSoutenanceByPFA(pfaId);
    }

    public LiveData<Convention> getPFAConvention(Long pfaId) {
        return conventionDao.getConventionByPFA(pfaId);
    }

    public LiveData<Integer> countDeliverables(Long pfaId) {
        return deliverableDao.getCountByPfaId(pfaId);
    }

    public LiveData<Evaluation> getPFAEvaluation(Long pfaId) {
        return evaluationDao.getByPfaIdLive(pfaId);
    }


}