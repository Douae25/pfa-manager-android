package ma.ensate.pfa_manager.repository;

import android.content.Context;
import android.content.SharedPreferences;


public class LanguageRepository {
    
    private static final String PREFS_NAME = "settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "fr";
    
    private final SharedPreferences sharedPreferences;
    
    public LanguageRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public String getSavedLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }
    public void saveLanguage(String languageCode) {
        sharedPreferences.edit()
                .putString(KEY_LANGUAGE, languageCode)
                .apply();
    }
}
