package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;
import com.google.gson.Gson;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.UserRepository;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<User> userLoginStatus = new MutableLiveData<>();
    private MutableLiveData<User> loggedInUser = new MutableLiveData<>();
    private MutableLiveData<String> loginResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private UserRepository userRepository;
    private SharedPreferences preferences;

    public LoginViewModel(UserRepository userRepository, Application application) {
        this.userRepository = userRepository;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

    public LiveData<User> getUserLoginStatus() {
        return userLoginStatus;
    }

    public LiveData<User> getLoggedInUser() {
        return loggedInUser;
    }

    public LiveData<String> getLoginResult() {
        return loginResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Veuillez remplir tous les champs");
            return;
        }

        userRepository.login(email, password, user -> {
            if (user != null) {
                userLoginStatus.postValue(user);
                // Sauvegarder l'utilisateur connecté
                saveLoggedInUser(user);
                loggedInUser.postValue(user);
                loginResult.postValue("Success");
            } else {
                errorMessage.postValue("Identifiants incorrects");
            }
        });
    }

    private void saveLoggedInUser(User user) {
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        preferences.edit()
            .putString("logged_in_user", userJson)
            .putLong("user_id", user.getUser_id())
            .putString("user_role", user.getRole().toString())
            .apply();
    }

    public User getLoggedInUserFromPreferences() {
        String userJson = preferences.getString("logged_in_user", null);
        if (userJson != null) {
            Gson gson = new Gson();
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public String getUserRole() {
        return preferences.getString("user_role", null);
    }

    public void logout() {
        preferences.edit()
            .remove("logged_in_user")
            .remove("user_id")
            .remove("user_role")
            .apply();
    }
}
    
    /**
     * Login avec Google : vérifie uniquement l'email dans la BD locale
     */
    public void loginWithGoogleEmail(String email) {
        if (email == null || email.isEmpty()) {
            errorMessage.setValue("Email Google invalide");
            return;
        }
        
        userRepository.getUserByEmail(email, user -> {
            if (user != null) {
                userLoginStatus.postValue(user);
            } else {
                errorMessage.postValue("Aucun compte trouvé pour " + email + ". Contactez l'administrateur.");
            }
        });
    }
}
