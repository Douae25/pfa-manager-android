package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.DatabaseInitializer;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.viewmodel.LoginViewModel;
import ma.ensate.pfa_manager.viewmodel.LoginViewModelFactory;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;


public class MainActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialiser la base de données avec les données par défaut
        DatabaseInitializer.initializeDatabase(getApplication());

        // Synchronisation automatique dès l'ouverture de l'app : uploadAll puis syncAll
        new Thread(() -> {
            ma.ensate.pfa_manager.sync.SyncManager.uploadAll(this);
            ma.ensate.pfa_manager.sync.SyncManager.syncAll(this);
        }).start();

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
        try {
            UserRepository userRepository = new UserRepository(getApplication());
            LoginViewModelFactory loginFactory = new LoginViewModelFactory(userRepository, getApplication());
            loginViewModel = new ViewModelProvider(this, loginFactory).get(LoginViewModel.class);

            TextInputEditText emailInput = findViewById(R.id.emailInput);
            TextInputEditText passwordInput = findViewById(R.id.passwordInput);
            Button loginBtn = findViewById(R.id.loginBtn);

            loginBtn.setOnClickListener(v -> {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                Log.d("LOGIN", "Attempting login with: " + email);
                loginViewModel.login(email, password);
            });

            loginViewModel.getLoginResult().observe(this, result -> {
                Log.d("LOGIN", "Login result: " + result);
                if ("Success".equals(result)) {
                    Toast.makeText(this, "Bienvenue !", Toast.LENGTH_SHORT).show();
                    // Délai court pour attendre la sauvegarde des préférences
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        User loggedInUser = loginViewModel.getLoggedInUserFromPreferences();
                        Log.d("LOGIN", "User after login: " + (loggedInUser != null ? loggedInUser.getEmail() + " (" + loggedInUser.getRole() + ")" : "null"));
                        redirectUserBasedOnRole(loggedInUser);
                    }, 500);
                } else {
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("LOGIN", "Error in setupLoginForm", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void redirectUserBasedOnRole(User user) {
        try {
            if (user != null && user.getRole() == Role.ADMIN) {
                Log.d("LOGIN", "Redirecting to AdminActivity");
                // Rediriger vers AdminActivity
                Intent adminIntent = new Intent(MainActivity.this, AdminActivity.class);
                adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(adminIntent);
                finish();
            } else {
                Log.d("LOGIN", "Redirecting to HomeActivity");
                // Rediriger vers HomeActivity pour les autres rôles
                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeIntent);
                finish();
            }
        } catch (Exception e) {
            Log.e("LOGIN", "Error in redirectUserBasedOnRole", e);
            Toast.makeText(this, "Erreur redirection: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
