package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
// Imports combinés
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.DatabaseInitializer;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.sync.SyncManager;
import ma.ensate.pfa_manager.util.TestDataHelper;
import ma.ensate.pfa_manager.view.etudiant.StudentSpaceActivity;
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
        
        // Defer login setup to avoid main thread congestion
        new Handler(Looper.getMainLooper()).postDelayed(this::setupLoginForm, 300);
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
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("LOGIN", "Attempting login with: " + email);
            loginViewModel.login(email, password);
        });

        loginViewModel.getUserLoginStatus().observe(this, user -> {
            if (user != null) {
                String displayName = (user.getFirst_name() != null && !user.getFirst_name().isEmpty()) 
                    ? user.getFirst_name() 
                    : user.getEmail();
                Toast.makeText(this, "Bienvenue " + displayName, Toast.LENGTH_SHORT).show();
            }
        });

        loginViewModel.getLoginResult().observe(this, result -> {
            Log.d("LOGIN", "Login result: " + result);
            if ("Success".equals(result)) {
                Toast.makeText(this, "Bienvenue !", Toast.LENGTH_SHORT).show();
                // Délai court pour attendre la sauvegarde des préférences
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    User loggedInUser = loginViewModel.getLoggedInUserFromPreferences();
                    Log.d("LOGIN", "User after login: " + (loggedInUser != null ? loggedInUser.getEmail() + " (" + loggedInUser.getRole() + ")" : "null"));
                    if (loggedInUser != null) {
                        redirectUser(loggedInUser);
                    } else {
                        Toast.makeText(this, "Utilisateur introuvable après connexion", Toast.LENGTH_SHORT).show();
                    }
                }, 500);
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            }
        });
        
        loginViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        } catch (Exception e) {
            Log.e("LOGIN", "Error setting up login form", e);
            Toast.makeText(this, "Erreur lors de la configuration du formulaire de connexion", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectUser(User user) {
        Intent intent = null;

        switch (user.getRole()) {
            case PROFESSOR:
                intent = new Intent(this, EncadrantDashboardActivity.class);
                break;
            case STUDENT:
                // 🔄 Démarrer la synchronisation des données de l'étudiant
                SyncManager syncManager = SyncManager.getInstance(getApplication());
                syncManager.syncUserDataFromBackend(user.getUser_id());
                
                intent = new Intent(this, StudentSpaceActivity.class);
                intent.putExtra("user", user);
                break;
            case ADMIN:
                intent = new Intent(this, AdminActivity.class);
                intent.putExtra("user", user);
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

    // redirectUserBasedOnRole was replaced by redirectUser for all roles to avoid returning to HomeActivity
    
   /* private void insertTestUserIfNeeded() {
        UserRepository userRepository = new UserRepository(getApplication());
        PFADossierRepository pfaDossierRepository = new PFADossierRepository(getApplication());
        
        userRepository.getUserByEmail("student@ensa.ma", user -> {
            if (user == null) {
                User testUser = new User();
                testUser.setEmail("student@ensa.ma");
                testUser.setPassword("student123");
                testUser.setFirst_name("Ahmed");
                testUser.setLast_name("Alami");
                testUser.setRole(Role.STUDENT);
                testUser.setPhone_number("0612345678");
                testUser.setCreated_at(System.currentTimeMillis());
                
                userRepository.insert(testUser, insertedUser -> {
                    PFADossier pfaDossier = new PFADossier();
                    pfaDossier.setStudent_id(insertedUser.getUser_id());
                    pfaDossier.setTitle("Test PFA Project");
                    pfaDossier.setDescription("Test project for student");
                    pfaDossier.setCurrent_status(PFAStatus.ASSIGNED);
                    pfaDossier.setUpdated_at(System.currentTimeMillis());
                    
                    pfaDossierRepository.insert(pfaDossier, createdDossier -> {
                        runOnUiThread(() -> 
                            Toast.makeText(this, "Compte Étudiant de test créé", Toast.LENGTH_SHORT).show()
                        );
                    });
                });
            }
        });
    }*/
}
