package ma.ensate.pfa_manager.view.coordinateur_filiere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.DepartmentDao;
import ma.ensate.pfa_manager.model.Department;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class CoordinatorDashboardActivity extends AppCompatActivity {

    private SettingsViewModel settingsViewModel;
    private long coordinatorId;
    private long departmentId;
    private DepartmentDao departmentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_dashboard);

        // Initialiser la BD
        departmentDao = AppDatabase.getInstance(this).departmentDao();

        Intent intent = getIntent();
        String userFirstName = "Coordinateur";
        String userLastName = "Filière";

        if (intent != null) {
            userFirstName = intent.getStringExtra("USER_FIRST_NAME");
            userLastName = intent.getStringExtra("USER_LAST_NAME");
            coordinatorId = intent.getLongExtra("USER_ID", -1);
            departmentId = intent.getLongExtra("USER_DEPARTMENT_ID", -1);

            if (userFirstName == null) userFirstName = "Coordinateur";
            if (userLastName == null) userLastName = "Filière";
        }

        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);

        settingsViewModel.applySavedLanguage();

        setupUI(userFirstName, userLastName);
        setupNavigation();
    }

    private void setupUI(String firstName, String lastName) {
        TextView tvCoordinatorName = findViewById(R.id.tvCoordinatorName);
        if (tvCoordinatorName != null) {
            String coordinatorName = String.format("%s %s", firstName, lastName);
            tvCoordinatorName.setText(coordinatorName);
        }

        TextView tvDepartmentWelcome = findViewById(R.id.tvDepartmentWelcome);
        if (tvDepartmentWelcome != null && departmentId != -1) {
            new Thread(() -> {
                Department department = departmentDao.getById(departmentId);
                runOnUiThread(() -> {
                    if (department != null) {
                        String welcomeMessage = String.format("Bienvenue Coordinateur - %s", department.getName());
                        tvDepartmentWelcome.setText(welcomeMessage);
                    } else {
                        tvDepartmentWelcome.setText("Bienvenue Coordinateur");
                    }
                });
            }).start();
        }

        View btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        if (btnLogoutIcon != null) {
            btnLogoutIcon.setOnClickListener(v -> finish());
        }

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

    private void setupNavigation() {
        LinearLayout btnManageStudents = findViewById(R.id.btnManageStudents);
        LinearLayout btnManageProfessors = findViewById(R.id.btnManageProfessors);
        LinearLayout btnManageAssignments = findViewById(R.id.btnManageAssignments);

        if (btnManageStudents != null) {
            btnManageStudents.setOnClickListener(v -> {
                Intent intent = new Intent(CoordinatorDashboardActivity.this, ManageStudentsActivity.class);
                intent.putExtra("COORDINATOR_ID", coordinatorId);
                intent.putExtra("DEPARTMENT_ID", departmentId);
                startActivity(intent);
            });
        }

        if (btnManageProfessors != null) {
            btnManageProfessors.setOnClickListener(v -> {
                Intent intent = new Intent(CoordinatorDashboardActivity.this, ManageProfessorsActivity.class);
                intent.putExtra("COORDINATOR_ID", coordinatorId);
                intent.putExtra("DEPARTMENT_ID", departmentId);
                startActivity(intent);
            });
        }

        if (btnManageAssignments != null) {
            btnManageAssignments.setOnClickListener(v -> {
                Intent intent = new Intent(CoordinatorDashboardActivity.this, ManageAssignmentsActivity.class);
                intent.putExtra("COORDINATOR_ID", coordinatorId);
                intent.putExtra("DEPARTMENT_ID", departmentId);
                startActivity(intent);
            });
        }
    }
}