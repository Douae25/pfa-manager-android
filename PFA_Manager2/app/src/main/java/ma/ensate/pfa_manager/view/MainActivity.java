package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.util.TestDataHelper;
import ma.ensate.pfa_manager.viewmodel.LoginViewModel;
import ma.ensate.pfa_manager.viewmodel.LoginViewModelFactory;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class MainActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TestDataHelper.insertTestData(this);
        setupLanguageToggle();
        setupBackNavigation();
        setupLoginForm();
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

    private void setupBackNavigation() {
        ImageView backArrow = findViewById(R.id.backArrow);
        if (backArrow != null) {
            backArrow.setVisibility(View.VISIBLE);
            backArrow.setOnClickListener(v -> finish());
        }
    }

    private void setupLoginForm() {
        UserRepository userRepository = new UserRepository(getApplication());
        LoginViewModelFactory loginFactory = new LoginViewModelFactory(userRepository);
        loginViewModel = new ViewModelProvider(this, loginFactory).get(LoginViewModel.class);

        TextInputEditText emailInput = findViewById(R.id.emailInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.login(email, password);
        });

        loginViewModel.getUserLoginStatus().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Bienvenue " + user.getFirst_name(), Toast.LENGTH_SHORT).show();
                redirectUser(user);
            }
        });

        loginViewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void redirectUser(User user) {
        Intent intent = null;

        switch (user.getRole()) {
            case PROFESSOR:
                intent = new Intent(this, EncadrantDashboardActivity.class);
                break;
            case STUDENT:
                // intent = new Intent(this, StudentDashboardActivity.class);
                break;
            case ADMIN:
                // intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case COORDINATOR:
                // intent = new Intent(this, CoordinatorDashboardActivity.class);
                break;
        }

        if (intent != null) {
            intent.putExtra("USER_ID", user.getUser_id());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Interface non disponible pour ce rôle", Toast.LENGTH_SHORT).show();
        }
    }
}