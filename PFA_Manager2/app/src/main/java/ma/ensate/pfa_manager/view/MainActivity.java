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
// Imports combinés
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;
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
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TestDataHelper.insertTestData(this); 
        insertTestUserIfNeeded();

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
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
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
    
    private void insertTestUserIfNeeded() {
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
    }
}