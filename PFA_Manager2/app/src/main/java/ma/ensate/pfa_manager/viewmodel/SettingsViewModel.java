package ma.ensate.pfa_manager.viewmodel;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.ViewModel;
import ma.ensate.pfa_manager.repository.LanguageRepository;


public class SettingsViewModel extends ViewModel {
    
    private final LanguageRepository languageRepository;
    
    public SettingsViewModel(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }
    
    public void applySavedLanguage() {
        String savedLanguage = languageRepository.getSavedLanguage();
        setAppLocale(savedLanguage);
    }
    
    public void changeLanguage(String languageCode) {
        languageRepository.saveLanguage(languageCode);
        setAppLocale(languageCode);
    }
    
    public String getCurrentLanguage() {
        return languageRepository.getSavedLanguage();
    }
    
    private void setAppLocale(String languageCode) {
        LocaleListCompat localeList = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(localeList);
    }
}
