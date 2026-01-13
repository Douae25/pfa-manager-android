package ma.ensate.pfa_manager.viewmodel;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.ViewModel;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import java.util.Locale;


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
        Locale locale;
        if ("fr".equalsIgnoreCase(languageCode)) {
            locale = new Locale("fr", "FR");
        } else if ("en".equalsIgnoreCase(languageCode)) {
            locale = new Locale("en", "US");
        } else {
            locale = new Locale(languageCode);
        }
        LocaleListCompat localeList = LocaleListCompat.forLanguageTags(locale.toLanguageTag());
        AppCompatDelegate.setApplicationLocales(localeList);
    }
}
