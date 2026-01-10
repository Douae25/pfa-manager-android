package ma.ensate.pfa_manager.repository;

import android.app.Application;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.ConventionDao;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;

public class ConventionRepository {

    private ConventionDao conventionDao;
    private ExecutorService executorService;

    public ConventionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        conventionDao = database.conventionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Insert Convention
    public void insert(Convention convention, OnConventionInsertedListener listener) {
        executorService.execute(() -> {
            long id = conventionDao.insert(convention);
            convention.setConvention_id(id);
            if (listener != null) {
                listener.onConventionInserted(convention);
            }
        });
    }

    public void updateConvention(Convention convention) {
        executorService.execute(() -> conventionDao.update(convention));
    }

    public void delete(Convention convention) {
        executorService.execute(() -> conventionDao.delete(convention));
    }

    public void getConventionById(long id, OnConventionFetchedListener listener) {
        executorService.execute(() -> {
            Convention convention = conventionDao.getById(id);
            if (listener != null) {
                listener.onConventionFetched(convention);
            }
        });
    }

    public void getConventionByPfaId(long pfaId, OnConventionFetchedListener listener) {
        executorService.execute(() -> {
            Convention convention = conventionDao.getByPfaId(pfaId);
            if (listener != null) {
                listener.onConventionFetched(convention);
            }
        });
    }

    public void getConventionsByState(ConventionState state, OnConventionsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Convention> conventions = conventionDao.getByState(state);
            if (listener != null) {
                listener.onConventionsListFetched(conventions);
            }
        });
    }

    public void getAllConventions(OnConventionsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Convention> conventions = conventionDao.getAll();
            if (listener != null) {
                listener.onConventionsListFetched(conventions);
            }
        });
    }

    public interface OnConventionInsertedListener {
        void onConventionInserted(Convention convention);
    }

    public interface OnConventionFetchedListener {
        void onConventionFetched(Convention convention);
    }

    public interface OnConventionsListFetchedListener {
        void onConventionsListFetched(List<Convention> conventions);
    }
}

