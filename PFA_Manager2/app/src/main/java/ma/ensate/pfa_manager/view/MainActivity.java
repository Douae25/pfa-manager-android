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
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
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

        // Insérer un utilisateur de test au démarrage
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
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            loginViewModel.login(email, password);
        });

        loginViewModel.getLoginResult().observe(this, result -> {
            if ("Success".equals(result)) {
                Toast.makeText(this, "Bienvenue !", Toast.LENGTH_SHORT).show();
                // Rediriger selon le rôle de l'utilisateur
                redirectBasedOnRole();
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void redirectBasedOnRole() {
        loginViewModel.getLoggedInUser().observe(this, user -> {
            if (user != null) {
                if (user.getRole() == Role.STUDENT) {
                    // Redirection vers l'espace étudiant
                    Intent intent = new Intent(MainActivity.this, StudentSpaceActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    finish();
                } 
            }
        });
    }
    
    private void insertTestUserIfNeeded() {
        UserRepository userRepository = new UserRepository(getApplication());
        
        // Vérifier si l'utilisateur existe déjà
        userRepository.getUserByEmail("student@ensa.ma", user -> {
            if (user == null) {
                // Créer un utilisateur de test
                User testUser = new User();
                testUser.setEmail("student@ensa.ma");
                testUser.setPassword("student123");
                testUser.setFirst_name("Ahmed");
                testUser.setLast_name("Alami");
                testUser.setRole(Role.STUDENT);
                testUser.setPhone_number("0612345678");
                testUser.setCreated_at(System.currentTimeMillis());
                
                // Insérer dans la base de données
                userRepository.insert(testUser, insertedUser -> {
                    runOnUiThread(() -> 
                        Toast.makeText(this, "Utilisateur de test créé: student@ensa.ma / student123", Toast.LENGTH_LONG).show()
                    );
                });
            }
        });
    }
}