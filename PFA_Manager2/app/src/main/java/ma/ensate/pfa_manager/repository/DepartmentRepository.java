package ma.ensate.pfa_manager.repository;

import android.app.Application;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.DepartmentDao;
import ma.ensate.pfa_manager.model.Department;

public class DepartmentRepository {
    private DepartmentDao departmentDao;
    private ExecutorService executorService;

    public DepartmentRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        departmentDao = database.departmentDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void getDepartmentById(long id, OnDepartmentFetchedListener listener) {
        executorService.execute(() -> {
            Department department = departmentDao.getById(id);
            if (listener != null) {
                listener.onDepartmentFetched(department);
            }
        });
    }

    public interface OnDepartmentFetchedListener {
        void onDepartmentFetched(Department department);
    }
}
