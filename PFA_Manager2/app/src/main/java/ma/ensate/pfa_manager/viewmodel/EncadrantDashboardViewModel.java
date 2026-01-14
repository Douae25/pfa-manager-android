package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import ma.ensate.pfa_manager.model.*;
import ma.ensate.pfa_manager.repository.DashboardRepository;
import ma.ensate.pfa_manager.repository.DeliverableRepository;
import ma.ensate.pfa_manager.repository.EvaluationRepository;
import ma.ensate.pfa_manager.repository.StudentRepository;
import ma.ensate.pfa_manager.repository.SoutenanceRepository;

public class EncadrantDashboardViewModel extends AndroidViewModel {

    private final DashboardRepository repository;
    private final StudentRepository studentRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final DeliverableRepository deliverableRepository;   // ← AJOUT
    private final EvaluationRepository evaluationRepository;     // ← AJOUT

    private final MutableLiveData<Long> supervisorIdLiveData = new MutableLiveData<>();

    // Données observables
    private final LiveData<User> currentUser;
    private final LiveData<List<User>> myStudents;
    private final LiveData<Integer> deliverablesCount;
    private final LiveData<Integer> soutenancesCount;
    private final LiveData<Integer> unplannedSoutenancesCount;
    private final LiveData<Integer> unevaluatedStudentsCount;

    // État de synchronisation
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public EncadrantDashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new DashboardRepository(application);
        studentRepository = new StudentRepository(application);
        soutenanceRepository = new SoutenanceRepository(application);
        deliverableRepository = new DeliverableRepository(application);   // ← AJOUT
        evaluationRepository = new EvaluationRepository(application);     // ← AJOUT

        currentUser = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getCurrentUser(id));

        myStudents = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getMyStudents(id));

        deliverablesCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getDeliverablesCount(id));

        soutenancesCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getSoutenancesCount(id));

        unplannedSoutenancesCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getUnplannedSoutenancesCount(id));

        unevaluatedStudentsCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getUnevaluatedStudentsCount(id));
    }

    public void setSupervisorId(Long supervisorId) {
        supervisorIdLiveData.setValue(supervisorId);

        // ══════════════════════════════════════════════════════════════
        // SYNC INITIALE AU LOGIN
        // ══════════════════════════════════════════════════════════════
        syncAllData(supervisorId);
    }

    /**
     * Synchronise TOUTES les données nécessaires au dashboard
     */
    private void syncAllData(Long supervisorId) {
        isSyncing.setValue(true);

        // 1. Sync étudiants + PFA (prioritaire car les autres en dépendent)
        studentRepository.refreshFromApi(supervisorId);

        // 2. Sync soutenances
        soutenanceRepository.syncFromApi(supervisorId);

        // 3. Sync livrables
        deliverableRepository.forceRefresh(supervisorId);

        // 4. Sync évaluations + critères
        evaluationRepository.forceRefresh(supervisorId);

        isSyncing.setValue(false);
    }

    /**
     * Permet de forcer un refresh manuel
     */
    public void refreshData() {
        Long supervisorId = supervisorIdLiveData.getValue();
        if (supervisorId != null) {
            syncAllData(supervisorId);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // GETTERS
    // ══════════════════════════════════════════════════════════════

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<User>> getMyStudents() {
        return myStudents;
    }

    public LiveData<Integer> getDeliverablesCount() {
        return deliverablesCount;
    }

    public LiveData<Integer> getSoutenancesCount() {
        return soutenancesCount;
    }

    public LiveData<Integer> getUnplannedSoutenancesCount() {
        return unplannedSoutenancesCount;
    }

    public LiveData<Integer> getUnevaluatedStudentsCount() {
        return unevaluatedStudentsCount;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }
}