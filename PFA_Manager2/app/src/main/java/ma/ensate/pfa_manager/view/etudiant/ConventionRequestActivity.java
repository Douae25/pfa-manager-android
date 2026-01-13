package ma.ensate.pfa_manager.view.etudiant;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.api.ConventionRequest;
import ma.ensate.pfa_manager.model.api.PFADossierRequest;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ConventionRequestActivity extends AppCompatActivity {

    private SettingsViewModel settingsViewModel;
    private User currentUser;
    private ConventionRepository conventionRepository;
    private PFADossierRepository pfaDossierRepository;
    
    private TextInputEditText inputFirstName, inputLastName, inputEmail, inputStudentNumber;
    private TextInputEditText inputCompanyName, inputCompanyAddress;
    private TextInputEditText inputSupervisorName, inputSupervisorEmail;
    private TextInputEditText inputStartDate, inputEndDate;
    private TextInputEditText inputProjectTitle, inputProjectDescription;
    private Button btnSubmitRequest;
    
    private Calendar selectedStartDate;
    private Calendar selectedEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
        
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convention_request);

        currentUser = (User) getIntent().getSerializableExtra("user");
        conventionRepository = new ConventionRepository(getApplication());
        pfaDossierRepository = new PFADossierRepository(getApplication());

        setupLanguageToggle();
        setupBackNavigation();
        initViews();
        prefillUserData();
        setupDatePickers();
        setupSubmitButton();
    }

    private void setupBackNavigation() {
        ImageView backArrow = findViewById(R.id.backArrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> finish());
        }
    }

    private void setupLanguageToggle() {
        // Language toggle removed - use parent activity for language change
    }

    private void initViews() {
        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputStudentNumber = findViewById(R.id.inputStudentNumber);
        inputCompanyName = findViewById(R.id.inputCompanyName);
        inputCompanyAddress = findViewById(R.id.inputCompanyAddress);
        inputSupervisorName = findViewById(R.id.inputSupervisorName);
        inputSupervisorEmail = findViewById(R.id.inputSupervisorEmail);
        inputStartDate = findViewById(R.id.inputStartDate);
        inputEndDate = findViewById(R.id.inputEndDate);
        inputProjectTitle = findViewById(R.id.inputProjectTitle);
        inputProjectDescription = findViewById(R.id.inputProjectDescription);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
    }

    private void prefillUserData() {
        if (currentUser != null) {
            inputFirstName.setText(currentUser.getFirst_name());
            inputLastName.setText(currentUser.getLast_name());
            inputEmail.setText(currentUser.getEmail());
            
            // Numéro étudiant basé sur l'ID (à adapter selon votre logique)
            inputStudentNumber.setText("ST" + String.format("%06d", currentUser.getUser_id()));
            
            // Désactiver les champs pré-remplis
            inputFirstName.setEnabled(false);
            inputLastName.setEnabled(false);
            inputEmail.setEnabled(false);
            inputStudentNumber.setEnabled(false);
        }
    }

    private void setupDatePickers() {
        selectedStartDate = Calendar.getInstance();
        selectedEndDate = Calendar.getInstance();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        inputStartDate.setOnClickListener(v -> {
            Calendar current = Calendar.getInstance();
            int year = current.get(Calendar.YEAR);
            
            // Définir les limites de dates (23 juin - 5 septembre)
            Calendar minDate = Calendar.getInstance();
            minDate.set(year, Calendar.JUNE, 23);
            Calendar maxDate = Calendar.getInstance();
            maxDate.set(year, Calendar.SEPTEMBER, 5);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, month, dayOfMonth) -> {
                    selectedStartDate.set(selectedYear, month, dayOfMonth);
                    inputStartDate.setText(dateFormat.format(selectedStartDate.getTime()));
                },
                current.get(Calendar.YEAR),
                Calendar.JUNE, // Mois par défaut: juin
                23 // Jour par défaut: 23
            );
            
            // Bloquer les dates hors intervalle
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });

        inputEndDate.setOnClickListener(v -> {
            Calendar current = Calendar.getInstance();
            int year = current.get(Calendar.YEAR);
            
            // Définir les limites de dates (23 juin - 5 septembre)
            Calendar minDate = Calendar.getInstance();
            minDate.set(year, Calendar.JUNE, 23);
            Calendar maxDate = Calendar.getInstance();
            maxDate.set(year, Calendar.SEPTEMBER, 5);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, month, dayOfMonth) -> {
                    selectedEndDate.set(selectedYear, month, dayOfMonth);
                    inputEndDate.setText(dateFormat.format(selectedEndDate.getTime()));
                },
                current.get(Calendar.YEAR),
                Calendar.SEPTEMBER, // Mois par défaut: septembre
                5 // Jour par défaut: 5
            );
            
            // Bloquer les dates hors intervalle
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
    }

    private boolean isValidDate(Calendar date) {
        int year = date.get(Calendar.YEAR);
        Calendar minDate = Calendar.getInstance();
        minDate.set(year, Calendar.JUNE, 23);
        
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(year, Calendar.SEPTEMBER, 5);
        
        return !date.before(minDate) && !date.after(maxDate);
    }

    private void setupSubmitButton() {
        btnSubmitRequest.setOnClickListener(v -> submitConventionRequest());
    }

    private void submitConventionRequest() {
        try {
            // Validation des champs obligatoires
            String companyName = inputCompanyName.getText().toString().trim();
            String companyAddress = inputCompanyAddress.getText().toString().trim();
            String supervisorName = inputSupervisorName.getText().toString().trim();
            String supervisorEmail = inputSupervisorEmail.getText().toString().trim();
            String startDate = inputStartDate.getText().toString().trim();
            String endDate = inputEndDate.getText().toString().trim();
            String projectTitle = inputProjectTitle.getText().toString().trim();
            String projectDescription = inputProjectDescription.getText().toString().trim();

            // Titre et description sont optionnels
            if (companyName.isEmpty() || companyAddress.isEmpty() || 
                supervisorName.isEmpty() || supervisorEmail.isEmpty() ||
                startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            // 📍 ÉTAPE 1 : Créer ou récupérer le dossier PFA via l'API
            PFADossierRequest pfaDossierRequest = new PFADossierRequest();
            pfaDossierRequest.setStudentId(currentUser.getUser_id());
            pfaDossierRequest.setTitle(projectTitle.isEmpty() ? null : projectTitle);
            pfaDossierRequest.setDescription(projectDescription.isEmpty() ? null : projectDescription);

            // Appeler l'API pour créer ou récupérer le dossier
            pfaDossierRepository.createOrGetPFADossier(pfaDossierRequest, pfaDossierResponse -> {
                try {
                    if (pfaDossierResponse != null) {
                        Long pfaId = pfaDossierResponse.getPfaId();
                        Log.d("ConventionRequest", "✅ PFA Dossier reçu/créé: pfaId=" + pfaId);
                        
                        // 📍 ÉTAPE 2 : Créer la convention avec le pfaId
                        createConvention(pfaId, companyName, companyAddress, 
                            supervisorName, supervisorEmail);
                    } else {
                        runOnUiThread(() -> Toast.makeText(ConventionRequestActivity.this, 
                            "Erreur: Impossible de créer le dossier PFA", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e("ConventionRequest", "Erreur dans createOrGetPFADossier callback", e);
                    runOnUiThread(() -> Toast.makeText(ConventionRequestActivity.this, 
                        "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
        } catch (Exception e) {
            Log.e("ConventionRequest", "Erreur dans submitConventionRequest", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void createConvention(Long pfaId, String companyName, String companyAddress,
                                   String supervisorName, String supervisorEmail) {
        try {
            // Créer la requête pour l'API
            ConventionRequest request = new ConventionRequest();
            request.setStudentId(currentUser.getUser_id());
            request.setPfaId(pfaId);
            request.setCompanyName(companyName);
            request.setCompanyAddress(companyAddress);
            request.setCompanySupervisorName(supervisorName);
            request.setCompanySupervisorEmail(supervisorEmail);
            request.setStartDate(selectedStartDate.getTimeInMillis());
            request.setEndDate(selectedEndDate.getTimeInMillis());

            // Appeler l'API avec la nouvelle méthode requestConvention()
            conventionRepository.requestConvention(request, new ConventionRepository.OnConventionRequestListener() {
                @Override
                public void onSuccess(Convention convention) {
                    Log.d("ConventionRequest", "Convention créée avec succès. ID: " + convention.getConvention_id());
                    runOnUiThread(() -> {
                        Toast.makeText(ConventionRequestActivity.this, 
                            "Demande de convention créée avec succès", Toast.LENGTH_LONG).show();
                        ConventionRequestActivity.this.finish();
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e("ConventionRequest", "Erreur API: " + error);
                    runOnUiThread(() -> Toast.makeText(ConventionRequestActivity.this, 
                        "Erreur serveur: " + error, Toast.LENGTH_LONG).show());
                }

                @Override
                public void onOffline(String message) {
                    Log.d("ConventionRequest", "Mode offline: " + message);
                    runOnUiThread(() -> {
                        Toast.makeText(ConventionRequestActivity.this, message, Toast.LENGTH_LONG).show();
                        ConventionRequestActivity.this.finish();
                    });
                }
            });
        } catch (Exception e) {
            Log.e("ConventionRequest", "Erreur dans createConvention", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
