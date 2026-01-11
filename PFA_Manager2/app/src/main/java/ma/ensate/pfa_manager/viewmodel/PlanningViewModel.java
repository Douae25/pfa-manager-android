package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.dto.PFAWithSoutenance;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.SoutenanceStatus;
import ma.ensate.pfa_manager.repository.SoutenanceRepository;

public class PlanningViewModel extends AndroidViewModel {

    private final SoutenanceRepository repository;
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>(false);

    public PlanningViewModel(@NonNull Application application) {
        super(application);
        repository = new SoutenanceRepository(application);
    }

    public LiveData<List<PFADossier>> getEligiblePFAs(Long supervisorId) {
        return repository.getPFAsEligibles(supervisorId);
    }

    public LiveData<List<PFAWithSoutenance>> getPFAsWithSoutenances(Long supervisorId) {
        return repository.getPFAsWithSoutenances(supervisorId);
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public void resetOperationSuccess() {
        operationSuccess.setValue(false);
    }

    public void planifierSoutenance(Long pfaId, String lieu, long dateSoutenance) {
        if (!validateInput(lieu, dateSoutenance)) return;

        Soutenance soutenance = new Soutenance();
        soutenance.setPfa_id(pfaId);
        soutenance.setLocation(lieu.trim());
        soutenance.setDate_soutenance(dateSoutenance);
        soutenance.setStatus(SoutenanceStatus.PLANNED);
        soutenance.setCreated_at(System.currentTimeMillis());

        repository.planifierSoutenance(soutenance, new SoutenanceRepository.OnSoutenanceListener() {
            @Override
            public void onSuccess(String message) {
                statusMessage.postValue(message);
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                statusMessage.postValue(message);
            }
        });
    }

    public void modifierSoutenance(Soutenance soutenance, String lieu, long dateSoutenance) {
        if (!validateInput(lieu, dateSoutenance)) return;

        soutenance.setLocation(lieu.trim());
        soutenance.setDate_soutenance(dateSoutenance);

        repository.modifierSoutenance(soutenance, new SoutenanceRepository.OnSoutenanceListener() {
            @Override
            public void onSuccess(String message) {
                statusMessage.postValue(message);
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                statusMessage.postValue(message);
            }
        });
    }

    public void supprimerSoutenance(long soutenanceId) {
        repository.supprimerSoutenance(soutenanceId, new SoutenanceRepository.OnSoutenanceListener() {
            @Override
            public void onSuccess(String message) {
                statusMessage.postValue(message);
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                statusMessage.postValue(message);
            }
        });
    }

    private boolean validateInput(String lieu, long dateSoutenance) {
        if (lieu == null || lieu.trim().isEmpty()) {
            statusMessage.setValue("Le lieu est obligatoire.");
            return false;
        }
        if (dateSoutenance <= System.currentTimeMillis()) {
            statusMessage.setValue("La date doit être ultérieure à aujourd'hui.");
            return false;
        }
        return true;
    }
}