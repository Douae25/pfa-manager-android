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

public class EncadrantDashboardViewModel extends AndroidViewModel {

    private final DashboardRepository repository;
    private final MutableLiveData<Long> supervisorIdLiveData = new MutableLiveData<>();

    // Données observables
    private final LiveData<User> currentUser;
    private final LiveData<List<User>> myStudents;
    private final LiveData<Integer> pfaCount;
    private final LiveData<Integer> deliverablesCount;
    private final LiveData<Integer> soutenancesCount;
    private final LiveData<Integer> conventionsToValidateCount;
    private final LiveData<List<Soutenance>> upcomingSoutenances;

    public EncadrantDashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new DashboardRepository(application);

        // Initialiser les LiveData avec transformations
        currentUser = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getCurrentUser(id));

        myStudents = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getMyStudents(id));

        pfaCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getPFACount(id));

        deliverablesCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getDeliverablesCount(id));

        soutenancesCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getSoutenancesCount(id));

        conventionsToValidateCount = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getConventionsToValidateCount(id));

        upcomingSoutenances = Transformations.switchMap(supervisorIdLiveData,
                id -> repository.getUpcomingSoutenances(id));
    }

    // Setter pour l'ID du superviseur
    public void setSupervisorId(Long supervisorId) {
        supervisorIdLiveData.setValue(supervisorId);
    }

    // Getters pour les LiveData
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<User>> getMyStudents() {
        return myStudents;
    }

    public LiveData<Integer> getPfaCount() {
        return pfaCount;
    }

    public LiveData<Integer> getDeliverablesCount() {
        return deliverablesCount;
    }

    public LiveData<Integer> getSoutenancesCount() {
        return soutenancesCount;
    }

    public LiveData<Integer> getConventionsToValidateCount() {
        return conventionsToValidateCount;
    }

    public LiveData<List<Soutenance>> getUpcomingSoutenances() {
        return upcomingSoutenances;
    }

    // Calculer le nombre total de notifications
    public LiveData<Integer> getTotalNotifications() {
        return Transformations.switchMap(supervisorIdLiveData, id -> {
            MutableLiveData<Integer> totalNotifs = new MutableLiveData<>();

            // Combiner les compteurs
            LiveData<Integer> conventions = repository.getConventionsToValidateCount(id);
            LiveData<Integer> deliverables = repository.getDeliverablesCount(id);

            // Observer et sommer
            conventions.observeForever(conv -> {
                Integer del = deliverables.getValue();
                totalNotifs.setValue((conv != null ? conv : 0) + (del != null ? del : 0));
            });

            deliverables.observeForever(del -> {
                Integer conv = conventions.getValue();
                totalNotifs.setValue((conv != null ? conv : 0) + (del != null ? del : 0));
            });

            return totalNotifs;
        });
    }
}