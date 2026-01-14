package ma.ensate.pfa_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.UserRepository;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<User> userLoginStatus = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private UserRepository userRepository;

    public LoginViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<User> getUserLoginStatus() {
        return userLoginStatus;
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
            } else {
                errorMessage.postValue("Identifiants incorrects");
            }
        });
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