package ma.ensate.pfa_manager.view.etudiant;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

        // Vérifier si l'étudiant a déjà un dossier PFA
        pfaDossierRepository.getByStudentId(currentUser.getUser_id(), existingDossier -> {
            if (existingDossier != null) {
                // Le dossier existe déjà, mettre à jour son statut et créer la convention
                existingDossier.setCurrent_status(PFAStatus.CONVENTION_PENDING);
                existingDossier.setUpdated_at(System.currentTimeMillis());
                pfaDossierRepository.update(existingDossier);
                
                createConvention(existingDossier.getPfa_id(), companyName, companyAddress, 
                    supervisorName, supervisorEmail);
            } else {
                // Créer d'abord le dossier PFA avec statut CONVENTION_PENDING
                PFADossier pfaDossier = new PFADossier();
                pfaDossier.setStudent_id(currentUser.getUser_id());
                pfaDossier.setTitle(projectTitle.isEmpty() ? null : projectTitle);
                pfaDossier.setDescription(projectDescription.isEmpty() ? null : projectDescription);
                pfaDossier.setCurrent_status(PFAStatus.CONVENTION_PENDING);
                pfaDossier.setUpdated_at(System.currentTimeMillis());

                // Insérer le dossier PFA
                pfaDossierRepository.insert(pfaDossier, insertedDossier -> {
                    // Puis créer la convention liée à ce dossier
                    createConvention(insertedDossier.getPfa_id(), companyName, companyAddress, 
                        supervisorName, supervisorEmail);
                });
            }
        });
    }

    private void createConvention(Long pfaId, String companyName, String companyAddress,
                                   String supervisorName, String supervisorEmail) {
        // Créer l'objet Convention
        Convention convention = new Convention();
        convention.setPfa_id(pfaId);
        convention.setCompany_name(companyName);
        convention.setCompany_address(companyAddress);
        convention.setCompany_supervisor_name(supervisorName);
        convention.setCompany_supervisor_email(supervisorEmail);
        convention.setStart_date(selectedStartDate.getTimeInMillis());
        convention.setEnd_date(selectedEndDate.getTimeInMillis());
        convention.setState(ConventionState.PENDING);
        convention.setIs_validated(false);

        // Sauvegarder la convention dans la base de données
        conventionRepository.insert(convention, insertedConvention -> {
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.convention_request_success, Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }
}
