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

        studentsWithPFA = Transformations.switchMap(supervisorIdLiveData,
                supervisorId -> {
                    if (supervisorId == null) {
                        return new MutableLiveData<>(null);
                    }
                    isLoading.setValue(true);
                    LiveData<List<StudentWithPFA>> students =
                            repository.getStudentsWithPFABySupervisor(supervisorId);

                    students.observeForever(list -> isLoading.setValue(false));

                    return students;
                });
    }

    public void setSupervisorId(Long supervisorId) {
        if (supervisorId == null) {
            errorMessage.setValue("ID superviseur invalide");
            return;
        }
        supervisorIdLiveData.setValue(supervisorId);
    }


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