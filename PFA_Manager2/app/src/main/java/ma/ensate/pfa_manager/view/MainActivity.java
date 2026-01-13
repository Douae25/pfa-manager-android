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
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.view.coordinateur_filiere.CoordinatorDashboardActivity; // Import
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
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            loginViewModel.login(email, password);
        });

        loginViewModel.getLoginResult().observe(this, result -> {
            if ("Success".equals(result)) {
                Toast.makeText(this, "Bienvenue !", Toast.LENGTH_SHORT).show();

                // Récupérer l'utilisateur connecté et rediriger
                loginViewModel.getLoggedInUser().observe(this, user -> {
                    if (user != null && user.getRole() == Role.COORDINATOR) {
                        // Redirection vers le dashboard coordinateur
                        Intent intent = new Intent(MainActivity.this, CoordinatorDashboardActivity.class);
                        intent.putExtra("USER_ID", user.getUser_id());
                        intent.putExtra("USER_EMAIL", user.getEmail());
                        intent.putExtra("USER_FIRST_NAME", user.getFirst_name());
                        intent.putExtra("USER_LAST_NAME", user.getLast_name());
                        intent.putExtra("USER_DEPARTMENT_ID", user.getDepartment_id()); // NOUVEAU
                        startActivity(intent);
                        finish();
                    } else {
                        // Pour les autres rôles, gardez le comportement existant
                        Toast.makeText(this, "Redirection vers votre espace...", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }
}