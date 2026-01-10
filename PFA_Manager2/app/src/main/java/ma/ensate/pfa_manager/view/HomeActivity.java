package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;


public class HomeActivity extends AppCompatActivity {

    private SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);

        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupNavigation();
        setupLanguageToggle();
    }

    private void setupNavigation() {
        Button btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void setupLanguageToggle() {
        TextView langFr = findViewById(R.id.langFr);
        TextView langEn = findViewById(R.id.langEn);
        langFr.setOnClickListener(v -> {
            settingsViewModel.changeLanguage("fr");
            recreate();
        });
        langEn.setOnClickListener(v -> {
            settingsViewModel.changeLanguage("en");
            recreate();
        });
    }
}
