package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.repository.DepartmentRepository;
import ma.ensate.pfa_manager.viewmodel.AdminViewModel;
import ma.ensate.pfa_manager.viewmodel.AdminViewModelFactory;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class ConventionDetailActivity extends AppCompatActivity {

    private AdminViewModel adminViewModel;
    private SettingsViewModel settingsViewModel;
    private TextView tvCompanyName, tvSupervisor, tvDates, tvState, tvAdminComment, tvStudentDepartment;
    private Button btnViewPdf;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup language first
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory settingsFactory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, settingsFactory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convention_detail);

        setupLanguageToggle();

        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvSupervisor = findViewById(R.id.tvSupervisor);
        tvDates = findViewById(R.id.tvDates);
        tvState = findViewById(R.id.tvState);
        tvAdminComment = findViewById(R.id.tvAdminComment);
        tvStudentDepartment = findViewById(R.id.tvStudentDepartment);
        btnViewPdf = findViewById(R.id.btnViewPdf);
        userRepository = new UserRepository(getApplication());

        ConventionRepository conventionRepository = new ConventionRepository(getApplication());
        AdminViewModelFactory factory = new AdminViewModelFactory(conventionRepository, userRepository);
        adminViewModel = new ViewModelProvider(this, factory).get(AdminViewModel.class);

        long conventionId = getIntent().getLongExtra("convention_id", -1);
        if (conventionId == -1) {
            Toast.makeText(this, "Aucune convention sélectionnée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // fetch convention
        conventionRepository.getConventionById(conventionId, convention -> runOnUiThread(() -> {
            if (convention == null) {
                Toast.makeText(ConventionDetailActivity.this, "Convention introuvable", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            populate(convention);
        }));
    }

    private void setupLanguageToggle() {
        TextView langFr = findViewById(R.id.langFr);
        TextView langEn = findViewById(R.id.langEn);
        if (langFr != null) {
            langFr.setOnClickListener(v -> {
                settingsViewModel.changeLanguage("fr");
                recreate();
            });
        }
        if (langEn != null) {
            langEn.setOnClickListener(v -> {
                settingsViewModel.changeLanguage("en");
                recreate();
            });
        }
    }

    private void populate(Convention c) {
        tvCompanyName.setText(getString(R.string.label_company) + ": " + (c.getCompany_name() == null ? getString(R.string.value_na) : c.getCompany_name()));
        String supervisor = getString(R.string.label_supervisor) + ": " + (c.getCompany_supervisor_name() == null ? getString(R.string.value_na) : c.getCompany_supervisor_name())
                + " (" + (c.getCompany_supervisor_email() == null ? getString(R.string.value_na) : c.getCompany_supervisor_email()) + ")";
        tvSupervisor.setText(supervisor);
        String address = getString(R.string.label_address) + ": " + (c.getCompany_address() == null ? getString(R.string.value_na) : c.getCompany_address());
        String dates = getString(R.string.label_period) + ": " + (c.getStart_date() == null ? getString(R.string.value_na) : new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(c.getStart_date())))
                + " " + getString(R.string.label_to) + " " + (c.getEnd_date() == null ? getString(R.string.value_na) : new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(c.getEnd_date())));
        tvDates.setText(dates);

        // Suppression de l'affichage du state et du commentaire admin
        tvState.setVisibility(View.GONE);
        tvAdminComment.setVisibility(View.GONE);
        // Récupérer le PFADossier via le repository
        PFADossierRepository pfaDossierRepository = new PFADossierRepository(getApplication());
        pfaDossierRepository.getPFADossierById(c.getPfa_id(), pfaDossier -> runOnUiThread(() -> {
            if (pfaDossier != null) {
                userRepository.getUserById(pfaDossier.getStudent_id(), user -> runOnUiThread(() -> {
                    if (user != null && user.getDepartment_id() != null) {
                        DepartmentRepository departmentRepository = new DepartmentRepository(getApplication());
                        departmentRepository.getDepartmentById(user.getDepartment_id(), department -> runOnUiThread(() -> {
                            if (department != null && department.getName() != null) {
                                tvStudentDepartment.setText(getString(R.string.label_department) + ": " + department.getName());
                            } else {
                                tvStudentDepartment.setText(getString(R.string.label_department) + ": N/A");
                            }
                            tvStudentDepartment.setVisibility(View.VISIBLE);
                        }));
                    } else {
                        tvStudentDepartment.setText(getString(R.string.label_department) + ": N/A");
                        tvStudentDepartment.setVisibility(View.VISIBLE);
                    }
                }));
            } else {
                tvStudentDepartment.setText(getString(R.string.label_department) + ": N/A");
                tvStudentDepartment.setVisibility(View.VISIBLE);
            }
        }));

        // Show PDF button ONLY for UPLOADED (signed) conventions
        String uri = c.getScanned_file_uri();
        boolean isSigned = c.getState() != null && c.getState().equals(ma.ensate.pfa_manager.model.ConventionState.UPLOADED);
        
        if (isSigned && uri != null && !uri.trim().isEmpty()) {
            btnViewPdf.setVisibility(View.VISIBLE);
            btnViewPdf.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri fileUri = Uri.parse(uri);
                    intent.setDataAndType(fileUri, "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("ConventionDetail", "Failed to open PDF", e);
                    Toast.makeText(ConventionDetailActivity.this, "Impossible d'ouvrir le PDF", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            btnViewPdf.setVisibility(View.GONE);
        }
    }
}
