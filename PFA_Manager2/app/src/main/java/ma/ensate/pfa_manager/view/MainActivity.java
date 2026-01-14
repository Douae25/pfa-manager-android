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
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.DatabaseInitializer;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.sync.SyncManager;
import ma.ensate.pfa_manager.util.TestDataHelper;
import ma.ensate.pfa_manager.view.coordinateur_filiere.CoordinatorDashboardActivity;
import ma.ensate.pfa_manager.view.etudiant.StudentSpaceActivity;
import ma.ensate.pfa_manager.viewmodel.LoginViewModel;
import ma.ensate.pfa_manager.viewmodel.LoginViewModelFactory;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private LoginViewModel loginViewModel;
    private SettingsViewModel settingsViewModel;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialisation DB
        DatabaseInitializer.initializeDatabase(getApplication());

        // Synchronisation globale
        new Thread(() -> {
            SyncManager.uploadAll(this);
            SyncManager.syncAll(this);
        }).start();

        // Langue
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory settingsFactory =
                new SettingsViewModelFactory(languageRepository);
        settingsViewModel =
                new ViewModelProvider(this, settingsFactory).get(SettingsViewModel.class);

        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TestDataHelper.insertTestData(this);
        // insertTestUserIfNeeded();
        
        // Initialiser Credential Manager pour Google Sign-In
        credentialManager = CredentialManager.create(this);

        setupLanguageToggle();
        setupBackNavigation();

        new Handler(Looper.getMainLooper()).postDelayed(
                this::setupLoginForm, 300
        );
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
        LoginViewModelFactory loginFactory =
                new LoginViewModelFactory(userRepository, getApplication());
        loginViewModel =
                new ViewModelProvider(this, loginFactory).get(LoginViewModel.class);

        TextInputEditText emailInput = findViewById(R.id.emailInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBtn = findViewById(R.id.loginBtn);
        android.widget.LinearLayout googleSignInBtn = findViewById(R.id.googleSignInBtn);

        // Login classique avec email/password
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Veuillez remplir tous les champs",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.login(email, password);
        });
        
        // Google Sign-In avec Credential Manager
        googleSignInBtn.setOnClickListener(v -> initiateGoogleSignIn());

        // Résultat login
        loginViewModel.getLoginResult().observe(this, result -> {
            if ("Success".equals(result)) {

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    User user = loginViewModel.getLoggedInUserFromPreferences();
                    if (user != null) {
                        redirectUser(user);
                    } else {
                        Toast.makeText(this,
                                "Utilisateur introuvable",
                                Toast.LENGTH_SHORT).show();
                    }
                }, 300);

            } else if (result != null) {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            }
        });

        loginViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void initiateGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.google_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null, // cancellationSignal
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Google Sign-In failed", e);
                            Toast.makeText(MainActivity.this, 
                                "Erreur Google Sign-In: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }
    
    private void handleGoogleSignInResult(GetCredentialResponse result) {
        try {
            GoogleIdTokenCredential googleIdTokenCredential = 
                GoogleIdTokenCredential.createFrom(result.getCredential().getData());
            
            String email = googleIdTokenCredential.getId();
            String displayName = googleIdTokenCredential.getDisplayName();
            
            Log.d(TAG, "Google Sign-In success: " + email);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Connexion Google: " + displayName, Toast.LENGTH_SHORT).show();
                // Vérifier l'email dans la BD locale
                loginViewModel.loginWithGoogleEmail(email);
            });
            
        } catch (Exception e) {
            runOnUiThread(() -> {
                Log.e(TAG, "Invalid Google ID token", e);
                Toast.makeText(this, "Token Google invalide: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * ✅ REDIRECTION UNIQUE POUR TOUS LES RÔLES
     */
    private void redirectUser(User user) {

        Intent intent;

        switch (user.getRole()) {

            case STUDENT:
                SyncManager.getInstance(getApplication())
                        .syncUserDataFromBackend(user.getUser_id());

                intent = new Intent(this, StudentSpaceActivity.class);
                intent.putExtra("user", user);
                break;

            case PROFESSOR:
                intent = new Intent(this, EncadrantDashboardActivity.class);
                break;

            case ADMIN:
                intent = new Intent(this, AdminActivity.class);
                break;

            case COORDINATOR:
                intent = new Intent(this, CoordinatorDashboardActivity.class);
                intent.putExtra("USER_DEPARTMENT_ID", user.getDepartment_id());
                break;

            default:
                Toast.makeText(this,
                        "Rôle non reconnu",
                        Toast.LENGTH_SHORT).show();
                return;
        }

        intent.putExtra("USER_ID", user.getUser_id());
        intent.putExtra("USER_EMAIL", user.getEmail());
        intent.putExtra("USER_FIRST_NAME", user.getFirst_name());
        intent.putExtra("USER_LAST_NAME", user.getLast_name());

        startActivity(intent);
        finish();
    }
}
