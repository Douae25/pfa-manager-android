package ma.ensate.pfa_manager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.dto.StudentWithPFA;

public class StudentRepository {

    private final UserDao userDao;

    public StudentRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    public LiveData<List<StudentWithPFA>> getStudentsWithPFABySupervisor(Long supervisorId) {
        return userDao.getStudentsWithPFABySupervisor(supervisorId);
    }
}