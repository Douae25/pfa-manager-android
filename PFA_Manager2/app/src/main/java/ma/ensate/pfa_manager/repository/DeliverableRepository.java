package ma.ensate.pfa_manager.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.DeliverableDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.dto.DeliverableWithStudent;

public class DeliverableRepository {

    private final DeliverableDao deliverableDao;
    private final PFADossierDao pfaDao;
    private final UserDao userDao;
    private final ExecutorService executor;

    public DeliverableRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        deliverableDao = db.deliverableDao();
        pfaDao = db.pfaDossierDao();
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insert(Deliverable deliverable) {
        executor.execute(() -> deliverableDao.insert(deliverable));
    }

    public void update(Deliverable deliverable) {
        executor.execute(() -> deliverableDao.update(deliverable));
    }

    public void delete(Deliverable deliverable) {
        executor.execute(() -> deliverableDao.delete(deliverable));
    }

    public LiveData<Deliverable> getById(Long id) {
        return deliverableDao.getById(id);
    }

    public LiveData<List<Deliverable>> getByPfaId(Long pfaId) {
        return deliverableDao.getByPfaId(pfaId);
    }

    public LiveData<List<Deliverable>> getBySupervisorId(Long supervisorId) {
        return deliverableDao.getBySupervisorId(supervisorId);
    }

    public LiveData<Integer> getCountBySupervisor(Long supervisorId) {
        return deliverableDao.getCountBySupervisor(supervisorId);
    }

    public LiveData<List<DeliverableWithStudent>> getDeliverablesWithStudents(Long supervisorId) {
        MediatorLiveData<List<DeliverableWithStudent>> result = new MediatorLiveData<>();

        LiveData<List<Deliverable>> deliverablesLiveData = deliverableDao.getBySupervisorId(supervisorId);
        LiveData<List<PFADossier>> pfasLiveData = pfaDao.getPFAsBySupervisor(supervisorId);
        LiveData<List<User>> studentsLiveData = userDao.getStudentsBySupervisor(supervisorId);

        result.addSource(deliverablesLiveData, deliverables -> {
            combineData(result, deliverables, pfasLiveData.getValue(), studentsLiveData.getValue());
        });

        result.addSource(pfasLiveData, pfas -> {
            combineData(result, deliverablesLiveData.getValue(), pfas, studentsLiveData.getValue());
        });

        result.addSource(studentsLiveData, students -> {
            combineData(result, deliverablesLiveData.getValue(), pfasLiveData.getValue(), students);
        });

        return result;
    }

    private void combineData(MediatorLiveData<List<DeliverableWithStudent>> result,
                             List<Deliverable> deliverables,
                             List<PFADossier> pfas,
                             List<User> students) {
        if (deliverables == null || pfas == null || students == null) {
            return;
        }

        Map<Long, PFADossier> pfaMap = new HashMap<>();
        for (PFADossier pfa : pfas) {
            pfaMap.put(pfa.getPfa_id(), pfa);
        }

        Map<Long, User> studentMap = new HashMap<>();
        for (User student : students) {
            studentMap.put(student.getUser_id(), student);
        }

        List<DeliverableWithStudent> combined = new ArrayList<>();
        for (Deliverable deliverable : deliverables) {
            PFADossier pfa = pfaMap.get(deliverable.getPfa_id());
            User student = null;
            if (pfa != null) {
                student = studentMap.get(pfa.getStudent_id());
            }
            combined.add(new DeliverableWithStudent(deliverable, pfa, student));
        }

        result.setValue(combined);
    }
}