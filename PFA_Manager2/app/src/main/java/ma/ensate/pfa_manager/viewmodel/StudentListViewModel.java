package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import ma.ensate.pfa_manager.model.dto.StudentWithPFA;
import ma.ensate.pfa_manager.repository.StudentRepository;

/**
 * ViewModel pour la liste des étudiants
 * Respecte MVVM : gère la logique UI et expose les données à l'Activity
 */
public class StudentListViewModel extends AndroidViewModel {

    private final StudentRepository repository;
    private final MutableLiveData<Long> supervisorIdLiveData = new MutableLiveData<>();
    private final LiveData<List<StudentWithPFA>> studentsWithPFA;

    // États UI
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public StudentListViewModel(@NonNull Application application) {
        super(application);
        repository = new StudentRepository(application);

        // Transforme supervisorId en liste d'étudiants
        studentsWithPFA = Transformations.switchMap(supervisorIdLiveData,
                supervisorId -> {
                    if (supervisorId == null) {
                        return new MutableLiveData<>(null);
                    }
                    isLoading.setValue(true);
                    LiveData<List<StudentWithPFA>> students =
                            repository.getStudentsWithPFABySupervisor(supervisorId);

                    // Observer pour mettre à jour isLoading
                    students.observeForever(list -> isLoading.setValue(false));

                    return students;
                });
    }

    /**
     * Définit l'ID du superviseur pour charger ses étudiants
     * @param supervisorId ID du superviseur connecté
     */
    public void setSupervisorId(Long supervisorId) {
        if (supervisorId == null) {
            errorMessage.setValue("ID superviseur invalide");
            return;
        }
        supervisorIdLiveData.setValue(supervisorId);
    }

    /**
     * Retourne la liste observable des étudiants avec leurs PFAs
     */
    public LiveData<List<StudentWithPFA>> getStudentsWithPFA() {
        return studentsWithPFA;
    }

    /**
     * État de chargement pour afficher un loader
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Messages d'erreur pour affichage UI
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Compte le nombre d'étudiants
     */
    public LiveData<Integer> getStudentCount() {
        return Transformations.map(studentsWithPFA,
                students -> students != null ? students.size() : 0);
    }

    /**
     * Filtre les étudiants par statut PFA (optionnel - pour plus tard)
     */
    public LiveData<List<StudentWithPFA>> filterByStatus(String status) {
        return Transformations.map(studentsWithPFA, students -> {
            if (students == null || status == null) return students;

            List<StudentWithPFA> filtered = new java.util.ArrayList<>();
            for (StudentWithPFA student : students) {
                if (status.equals(student.getPFAStatus())) {
                    filtered.add(student);
                }
            }
            return filtered;
        });
    }
}