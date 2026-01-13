package ma.ensate.pfa_manager.repository;

import android.app.Application;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.DepartmentDao;
import ma.ensate.pfa_manager.model.Department;

public class DepartmentCacheRepository {
    private DepartmentDao departmentDao;
    private ExecutorService executorService;
    private Map<Long, String> departmentNameCache = new HashMap<>();

    public DepartmentCacheRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        departmentDao = database.departmentDao();
        executorService = Executors.newSingleThreadExecutor();
        cacheAllDepartments();
    }

    private void cacheAllDepartments() {
        executorService.execute(() -> {
            List<Department> departments = departmentDao.getAll();
            for (Department d : departments) {
                departmentNameCache.put(d.getDepartment_id(), d.getName());
            }
        });
    }

    public void getDepartmentNameById(Long id, OnDepartmentNameFetchedListener listener) {
        executorService.execute(() -> {
            String name = departmentNameCache.get(id);
            if (listener != null) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> listener.onDepartmentNameFetched(name));
            }
        });
    }

    public interface OnDepartmentNameFetchedListener {
        void onDepartmentNameFetched(String name);
    }
}
