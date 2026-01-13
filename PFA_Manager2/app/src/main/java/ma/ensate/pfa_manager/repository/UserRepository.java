package ma.ensate.pfa_manager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.User;

public class UserRepository {

    private UserDao userDao;
    private ExecutorService executorService;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // ========== MÉTHODES ASYNCHRONES (avec Callbacks) ==========

    public void insert(User user, OnUserInsertedListener listener) {
        executorService.execute(() -> {
            long id = userDao.insert(user);
            user.setUser_id(id);
            if (listener != null) {
                listener.onUserInserted(user);
            }
        });
    }

    public void update(User user) {
        executorService.execute(() -> userDao.update(user));
    }

    public void delete(User user) {
        executorService.execute(() -> userDao.delete(user));
    }

    public void getUserById(Long userId, OnUserFetchedListener listener) {
        executorService.execute(() -> {
            if (userId != null) {
                User user = userDao.getUserByIdSync(userId);
                if (listener != null) {
                    listener.onUserFetched(user);
                }
            }
        });
    }

    public void getUserByEmail(String email, OnUserFetchedListener listener) {
        executorService.execute(() -> {
            User user = userDao.getUserByEmailSync(email);
            if (listener != null) {
                listener.onUserFetched(user);
            }
        });
    }

    public void login(String email, String password, OnUserFetchedListener listener) {
        executorService.execute(() -> {
            User user = userDao.login(email, password);
            if (listener != null) {
                listener.onUserFetched(user);
            }
        });
    }

    public void getAllUsers(OnUsersListFetchedListener listener) {
        executorService.execute(() -> {
            List<User> users = userDao.getAllUsers();
            if (listener != null) {
                listener.onUsersListFetched(users);
            }
        });
    }

    // ========== MÉTHODES LIVEDATA (pour observation UI) ==========

    public LiveData<User> getUserByIdLiveData(Long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<User> getUserByEmailLiveData(String email) {
        return userDao.getUserByEmail(email);
    }

    // ========== INTERFACES CALLBACKS ==========

    
    public void getUserById(long userId, OnUserFetchedListener listener) {
        executorService.execute(() -> {
            User user = userDao.getUserByIdSync(userId);
            if (listener != null) {
                listener.onUserFetched(user);
            }
        });
    }
    
    public interface OnUserInsertedListener {
        void onUserInserted(User user);
    }

    public interface OnUserFetchedListener {
        void onUserFetched(User user);
    }

    public interface OnUsersListFetchedListener {
        void onUsersListFetched(List<User> users);
    }
}