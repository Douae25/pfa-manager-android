package ma.ensate.pfa_manager.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import ma.ensate.pfa_manager.repository.UserRepository;

public class LoginViewModelFactory implements ViewModelProvider.Factory {
    
    private final UserRepository userRepository;
    
    public LoginViewModelFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
