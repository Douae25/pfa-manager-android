package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import ma.ensate.pfa_manager.repository.UserRepository;

public class LoginViewModelFactory implements ViewModelProvider.Factory {
    
    private final UserRepository userRepository;
    private final Application application;
    
    public LoginViewModelFactory(UserRepository userRepository, Application application) {
        this.userRepository = userRepository;
        this.application = application;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(userRepository, application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
