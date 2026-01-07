package ma.ensate.pfa_manager.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import ma.ensate.pfa_manager.repository.LanguageRepository;


public class SettingsViewModelFactory implements ViewModelProvider.Factory {
    
    private final LanguageRepository languageRepository;
    
    public SettingsViewModelFactory(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(languageRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
