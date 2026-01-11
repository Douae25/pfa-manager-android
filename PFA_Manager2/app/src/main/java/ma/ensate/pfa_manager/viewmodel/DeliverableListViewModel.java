package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import ma.ensate.pfa_manager.model.dto.DeliverableWithStudent;
import ma.ensate.pfa_manager.repository.DeliverableRepository;

public class DeliverableListViewModel extends AndroidViewModel {

    private final DeliverableRepository repository;
    private final MutableLiveData<Long> supervisorIdLiveData = new MutableLiveData<>();

    private final LiveData<List<DeliverableWithStudent>> deliverablesWithStudents;
    private final LiveData<Integer> deliverableCount;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DeliverableListViewModel(@NonNull Application application) {
        super(application);
        repository = new DeliverableRepository(application);

        deliverablesWithStudents = Transformations.switchMap(supervisorIdLiveData,
                id -> {
                    isLoading.setValue(true);
                    LiveData<List<DeliverableWithStudent>> data = repository.getDeliverablesWithStudents(id);
                    isLoading.setValue(false);
                    return data;
                });

        deliverableCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getCountBySupervisor(id));
    }

    public void setSupervisorId(Long supervisorId) {
        supervisorIdLiveData.setValue(supervisorId);
    }

    public LiveData<List<DeliverableWithStudent>> getDeliverablesWithStudents() {
        return deliverablesWithStudents;
    }

    public LiveData<Integer> getDeliverableCount() {
        return deliverableCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void deleteDeliverable(DeliverableWithStudent item) {
        if (item != null && item.deliverable != null) {
            repository.delete(item.deliverable);
        }
    }
}