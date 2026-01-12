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

public class CoordinatorStudentsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<User>> students = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final long coordinatorId;

    public CoordinatorStudentsViewModel(Application application, long coordinatorId) {
        super(application);
        this.coordinatorId = coordinatorId;
    }

    public LiveData<List<User>> getStudents() {
        return students;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadStudents() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplication());
                UserDao userDao = db.userDao();

                // 1. Trouver le département du coordinateur
                User coordinator = userDao.getUserById(coordinatorId);
                if (coordinator == null || coordinator.getDepartment_id() == null) {
                    errorMessage.postValue("Coordinateur non trouvé ou sans département");
                    return;
                }

                Long departmentId = coordinator.getDepartment_id();

                // 2. Récupérer tous les étudiants de ce département
                List<User> studentList = userDao.getUsersByDepartmentAndRole(departmentId, Role.STUDENT);

                students.postValue(studentList);

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