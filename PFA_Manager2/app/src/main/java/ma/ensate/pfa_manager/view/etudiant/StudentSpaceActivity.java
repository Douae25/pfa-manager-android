package ma.ensate.pfa_manager.view.etudiant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.view.MainActivity;
import ma.ensate.pfa_manager.view.etudiant.fragments.ConventionFragment;
import ma.ensate.pfa_manager.view.etudiant.fragments.DeliverablesFragment;
import ma.ensate.pfa_manager.view.etudiant.fragments.NoteFragment;
import ma.ensate.pfa_manager.view.etudiant.fragments.SoutenanceFragment;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class StudentSpaceActivity extends AppCompatActivity {

    private SettingsViewModel settingsViewModel;
    private User currentUser;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
        
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_space);

        // Récupérer l'utilisateur connecté depuis l'intent
        currentUser = (User) getIntent().getSerializableExtra("user");

        setupLanguageToggle();
        setupWelcomeMessage();
        setupLogoutButton();
        setupBottomNavigation();
        
        // Charger le fragment par défaut
        if (savedInstanceState == null) {
            loadFragment(ConventionFragment.newInstance(currentUser));
        }
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

    private void setupWelcomeMessage() {
        TextView welcomeText = findViewById(R.id.welcomeText);
        if (currentUser != null) {
            String fullName = currentUser.getFirst_name() + " " + currentUser.getLast_name();
            welcomeText.setText(getString(R.string.student_welcome, fullName));
        }
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Retour à la page de login
            Intent intent = new Intent(StudentSpaceActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_convention) {
                fragment = ConventionFragment.newInstance(currentUser);
            } else if (itemId == R.id.nav_deliverables) {
                fragment = DeliverablesFragment.newInstance(currentUser);
            } else if (itemId == R.id.nav_soutenance) {
                fragment = SoutenanceFragment.newInstance(currentUser);
            } else if (itemId == R.id.nav_note) {
                fragment = NoteFragment.newInstance(currentUser);
            }
            
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }
    
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
