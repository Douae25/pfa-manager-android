package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.UserRepository;

public class AdminViewModelFactory implements ViewModelProvider.Factory {

    private final ConventionRepository conventionRepository;
    private final UserRepository userRepository;

    public AdminViewModelFactory(ConventionRepository conventionRepository, UserRepository userRepository) {
        this.conventionRepository = conventionRepository;
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AdminViewModel.class)) {
            return (T) new AdminViewModel(conventionRepository, userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
