package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import ma.ensate.pfa_manager.model.dto.StudentWithPFA;
import ma.ensate.pfa_manager.repository.StudentRepository;

public class StudentListViewModel extends AndroidViewModel {

    private final StudentRepository repository;
    private final MutableLiveData<Long> supervisorIdLiveData = new MutableLiveData<>();

    // Données depuis Room (source unique de vérité)
    private final LiveData<List<StudentWithPFA>> studentsWithPFA;

    // États UI
    private final MediatorLiveData<Boolean> isLoading = new MediatorLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public StudentListViewModel(@NonNull Application application) {
        super(application);
        repository = new StudentRepository(application);

        // Observer les données de Room via switchMap
        studentsWithPFA = Transformations.switchMap(supervisorIdLiveData,
                supervisorId -> {
                    if (supervisorId == null || supervisorId == -1L) {
                        return new MutableLiveData<>(null);
                    }
                    // Retourne le LiveData de Room (qui sera auto-mis à jour après sync)
                    return repository.getStudentsWithPFABySupervisor(supervisorId);
                });

        // Observer l'état de synchronisation
        isLoading.addSource(repository.getIsSyncing(), isSyncing -> {
            isLoading.setValue(isSyncing);
        });

        // Observer les erreurs de sync
        repository.getSyncError().observeForever(error -> {
            if (error != null && !error.isEmpty()) {
                errorMessage.setValue(error);
            }
        });
    }

    public void setSupervisorId(Long supervisorId) {
        if (supervisorId == null || supervisorId == -1L) {
            errorMessage.setValue("ID superviseur invalide");
            return;
        }
        supervisorIdLiveData.setValue(supervisorId);
    }

    /**
     * Retourne les étudiants depuis Room (mis à jour automatiquement après sync API)
     */
    public LiveData<List<StudentWithPFA>> getStudentsWithPFA() {
        return studentsWithPFA;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getStudentCount() {
        return Transformations.map(studentsWithPFA,
                students -> students != null ? students.size() : 0);
    }

    /**
     * Pull-to-refresh : Force la synchronisation depuis l'API
     */
    public void refresh() {
        Long supervisorId = supervisorIdLiveData.getValue();
        if (supervisorId != null && supervisorId != -1L) {
            repository.forceRefresh(supervisorId);
        }
    }
}