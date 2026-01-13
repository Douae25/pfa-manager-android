package ma.ensate.pfa_manager.viewmodel.coordinateur_filiere;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ma.ensate.pfa_manager.model.ProfessorWithStats;
import ma.ensate.pfa_manager.repository.coordinateur_filiere.CoordinatorRepository;
import java.util.List;

public class ProfessorsWithStatsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ProfessorWithStats>> professors = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final CoordinatorRepository repository;
    private final long departmentId;

    public ProfessorsWithStatsViewModel(Application application, long departmentId) {
        super(application);
        this.repository = new CoordinatorRepository(application);
        this.departmentId = departmentId;
    }

    public LiveData<List<ProfessorWithStats>> getProfessors() {
        return professors;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadProfessorsWithStats() {
        repository.getProfessorsWithStats(departmentId, new CoordinatorRepository.Callback<List<ProfessorWithStats>>() {
            @Override
            public void onSuccess(List<ProfessorWithStats> result) {
                professors.postValue(result);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
            }
        });
    }
}