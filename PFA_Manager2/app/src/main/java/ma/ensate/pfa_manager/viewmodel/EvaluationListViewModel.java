package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import ma.ensate.pfa_manager.model.EvaluationCriteria;
import ma.ensate.pfa_manager.model.dto.CriteriaWithScore;
import ma.ensate.pfa_manager.model.dto.SoutenanceWithEvaluation;
import ma.ensate.pfa_manager.repository.EvaluationRepository;

public class EvaluationListViewModel extends AndroidViewModel {

    private final EvaluationRepository repository;
    private final MutableLiveData<Long> supervisorIdLiveData = new MutableLiveData<>();

    private final LiveData<List<SoutenanceWithEvaluation>> soutenancesWithEvaluations;
    private final LiveData<List<EvaluationCriteria>> activeCriteria;
    private final LiveData<Integer> evaluatedCount;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public EvaluationListViewModel(@NonNull Application application) {
        super(application);
        repository = new EvaluationRepository(application);

        soutenancesWithEvaluations = Transformations.switchMap(supervisorIdLiveData,
                id -> {
                    isLoading.setValue(true);
                    LiveData<List<SoutenanceWithEvaluation>> data = repository.getSoutenancesWithEvaluations(id);
                    isLoading.setValue(false);
                    return data;
                });

        evaluatedCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getEvaluatedCount(id));

        activeCriteria = repository.getActiveCriteria();
    }

    public void setSupervisorId(Long supervisorId) {
        supervisorIdLiveData.setValue(supervisorId);
    }

    public LiveData<List<SoutenanceWithEvaluation>> getSoutenancesWithEvaluations() {
        return soutenancesWithEvaluations;
    }

    public LiveData<List<EvaluationCriteria>> getActiveCriteria() {
        return activeCriteria;
    }

    public LiveData<Integer> getEvaluatedCount() {
        return evaluatedCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void saveEvaluation(Long pfaId, List<CriteriaWithScore> criteriaScores) {
        Long evaluatorId = supervisorIdLiveData.getValue();
        if (evaluatorId == null) return;

        repository.saveEvaluation(pfaId, evaluatorId, criteriaScores,
                new EvaluationRepository.OnEvaluationListener() {
                    @Override
                    public void onSuccess(String message) {
                        toastMessage.postValue(message);
                    }

                    @Override
                    public void onError(String message) {
                        toastMessage.postValue(message);
                    }
                });
    }

    public void clearToast() {
        toastMessage.setValue(null);
    }
}