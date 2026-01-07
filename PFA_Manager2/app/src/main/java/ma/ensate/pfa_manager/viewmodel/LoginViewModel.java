package ma.ensate.pfa_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.model.User;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<String> loginResult = new MutableLiveData<>();
    private MutableLiveData<User> loggedInUser = new MutableLiveData<>();
    private UserRepository userRepository;

    public LoginViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<String> getLoginResult() {
        return loginResult;
    }

    public LiveData<User> getLoggedInUser() {
        return loggedInUser;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            loginResult.setValue("Champs vides !");
            return;
        }

        // Recherche dans la base de données
        userRepository.login(email, password, user -> {
            if (user != null) {
                loggedInUser.postValue(user);
                loginResult.postValue("Success");
            } else {
                loginResult.postValue("Identifiants incorrects");
            }
        });
    }
}