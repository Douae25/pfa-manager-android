package ma.ensate.pfa_manager.view.coordinateur_filiere;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.view.coordinateur_filiere.adapter.ProfessorAssignmentAdapter;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class ManageAssignmentsActivity extends AppCompatActivity {

    private SettingsViewModel settingsViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textSummary;
    private View textEmpty;
    private Button btnAutoAssign;
    private ImageButton btnRefresh;

    private ProfessorAssignmentAdapter adapter;
    private long departmentId;

    private UserDao userDao;
    private PFADossierDao pfaDossierDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_assignments);

        departmentId = getIntent().getLongExtra("DEPARTMENT_ID", -1);

        AppDatabase db = AppDatabase.getInstance(this);
        userDao = db.userDao();
        pfaDossierDao = db.pfaDossierDao();

        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory settingsFactory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, settingsFactory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();

        setupUI();
        loadAssignments();
    }

    private void setupUI() {
        recyclerView = findViewById(R.id.recyclerViewAssignments);
        progressBar = findViewById(R.id.progressBar);
        textSummary = findViewById(R.id.textSummary);
        textEmpty = findViewById(R.id.textEmpty);
        btnAutoAssign = findViewById(R.id.btnAutoAssign);
        btnRefresh = findViewById(R.id.btnRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProfessorAssignmentAdapter();
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnAutoAssign.setOnClickListener(v -> showAutoAssignConfirmation());
        btnRefresh.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            loadAssignments();
        });

        setupLanguageToggle();
    }

    private void setupLanguageToggle() {
        View langFr = findViewById(R.id.langFr);
        View langEn = findViewById(R.id.langEn);
        if (langFr != null) langFr.setOnClickListener(v -> { settingsViewModel.changeLanguage("fr"); recreate(); });
        if (langEn != null) langEn.setOnClickListener(v -> { settingsViewModel.changeLanguage("en"); recreate(); });
    }

    private void loadAssignments() {
        new Thread(() -> {
            try {
                List<User> professors = userDao.getUsersByDepartmentAndRole(departmentId, Role.PROFESSOR);
                List<ProfessorAssignmentAdapter.ProfessorAssignment> assignments = new ArrayList<>();

                for (User professor : professors) {
                    List<PFADossier> pfaDossiers = pfaDossierDao.getBySupervisor(professor.getUser_id());
                    List<User> students = new ArrayList<>();
                    for (PFADossier dossier : pfaDossiers) {
                        User student = userDao.getUserById(dossier.getStudent_id());
                        if (student != null) students.add(student);
                    }
                    assignments.add(new ProfessorAssignmentAdapter.ProfessorAssignment(professor, students));
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (!assignments.isEmpty()) {
                        adapter.setAssignments(assignments);
                        recyclerView.setVisibility(View.VISIBLE);
                        textEmpty.setVisibility(View.GONE);
                        updateSummary(assignments);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        textEmpty.setVisibility(View.VISIBLE);
                        textSummary.setText("Aucun encadrant trouvé");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateSummary(List<ProfessorAssignmentAdapter.ProfessorAssignment> assignments) {
        int totalProfessors = assignments.size();
        int assignedCount = 0;
        for (ProfessorAssignmentAdapter.ProfessorAssignment a : assignments) assignedCount += a.getStudents().size();

        final int finalAssigned = assignedCount;
        new Thread(() -> {
            List<User> allStudents = userDao.getUsersByDepartmentAndRole(departmentId, Role.STUDENT);
            int total = allStudents.size();
            runOnUiThread(() -> textSummary.setText(totalProfessors + " encadrants • " + finalAssigned + "/" + total + " étudiants affectés"));
        }).start();
    }

    private void showAutoAssignConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Affectation automatique")
                .setMessage("Continuer l'affectation équitable ?")
                .setPositiveButton("Oui", (dialog, which) -> performAutoAssignment())
                .setNegativeButton("Non", null)
                .show();
    }

    private void performAutoAssignment() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                List<User> professors = userDao.getUsersByDepartmentAndRole(departmentId, Role.PROFESSOR);
                if (professors.isEmpty()) {
                    runOnUiThread(() -> { progressBar.setVisibility(View.GONE); Toast.makeText(this, "Aucun professeur !", Toast.LENGTH_SHORT).show(); });
                    return;
                }

                List<User> allStudents = userDao.getUsersByDepartmentAndRole(departmentId, Role.STUDENT);
                List<User> unassigned = new ArrayList<>();
                for (User s : allStudents) {
                    if (pfaDossierDao.getByStudent(s.getUser_id()).isEmpty()) unassigned.add(s);
                }

                if (unassigned.isEmpty()) {
                    runOnUiThread(() -> { progressBar.setVisibility(View.GONE); Toast.makeText(this, "Déjà tout affecté !", Toast.LENGTH_SHORT).show(); });
                    return;
                }

                Collections.shuffle(unassigned);
                int profIndex = 0;
                for (User student : unassigned) {
                    User prof = professors.get(profIndex % professors.size());
                    PFADossier d = new PFADossier();
                    d.setStudent_id(student.getUser_id());
                    d.setSupervisor_id(prof.getUser_id());
                    d.setTitle("PFA - " + student.getFirst_name());
                    d.setCurrent_status(PFAStatus.CONVENTION_PENDING);
                    d.setUpdated_at(System.currentTimeMillis());
                    pfaDossierDao.insert(d);
                    profIndex++;
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Succès !", Toast.LENGTH_LONG).show();
                    loadAssignments();
                });
            } catch (Exception e) {
                runOnUiThread(() -> { progressBar.setVisibility(View.GONE); Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show(); });
            }
        }).start();
    }
}