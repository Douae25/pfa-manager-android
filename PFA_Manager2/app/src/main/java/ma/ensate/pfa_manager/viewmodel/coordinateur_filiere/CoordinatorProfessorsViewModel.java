package ma.ensate.pfa_manager.viewmodel.coordinateur_filiere;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoordinatorProfessorsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<User>> professors = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final long departmentId;

    public CoordinatorProfessorsViewModel(Application application, long departmentId) {
        super(application);
        this.departmentId = departmentId;
    }

    public LiveData<List<User>> getProfessors() {
        return professors;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadProfessors() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplication());
                UserDao userDao = db.userDao();

                // Récupérer tous les professeurs de ce département
                List<User> professorList = userDao.getUsersByDepartmentAndRole(departmentId, Role.PROFESSOR);

                professors.postValue(professorList);

            } catch (Exception e) {
                errorMessage.postValue("Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}