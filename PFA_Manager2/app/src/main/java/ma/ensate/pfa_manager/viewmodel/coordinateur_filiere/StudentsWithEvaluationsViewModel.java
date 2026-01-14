package ma.ensate.pfa_manager.viewmodel.coordinateur_filiere;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ma.ensate.pfa_manager.model.StudentWithEvaluation;
import ma.ensate.pfa_manager.repository.coordinateur_filiere.CoordinatorRepository;
import java.util.List;

public class StudentsWithEvaluationsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<StudentWithEvaluation>> students = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final CoordinatorRepository repository;
    private final long departmentId;

    public StudentsWithEvaluationsViewModel(Application application, long departmentId) {
        super(application);
        this.repository = new CoordinatorRepository(application);
        this.departmentId = departmentId;
    }

    public LiveData<List<StudentWithEvaluation>> getStudents() {
        return students;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadStudentsWithEvaluations() {
        repository.getStudentsWithEvaluations(departmentId, new CoordinatorRepository.Callback<List<StudentWithEvaluation>>() {
            @Override
            public void onSuccess(List<StudentWithEvaluation> result) {
                students.postValue(result);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
            }
        });
    }
}