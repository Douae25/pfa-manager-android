package ma.ensate.pfa_manager.view.coordinateur_filiere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.ProfessorWithStats;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.view.coordinateur_filiere.adapter.PaginatedProfessorAdapter;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;
import ma.ensate.pfa_manager.viewmodel.coordinateur_filiere.ProfessorsWithStatsViewModel;
import ma.ensate.pfa_manager.viewmodel.coordinateur_filiere.ProfessorsWithStatsViewModelFactory;

import java.util.List;

public class ManageProfessorsActivity extends AppCompatActivity {

    private ProfessorsWithStatsViewModel viewModel;
    private SettingsViewModel settingsViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PaginatedProfessorAdapter adapter;
    private TextView textSummary;
    private TextView textPageInfo;
    private Button btnPrev, btnNext;
    private View layoutPagination;

    private long departmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_professors);

        Intent intent = getIntent();
        departmentId = intent.getLongExtra("DEPARTMENT_ID", -1);

        ProfessorsWithStatsViewModelFactory factory = new ProfessorsWithStatsViewModelFactory(getApplication(), departmentId);
        viewModel = new ViewModelProvider(this, factory).get(ProfessorsWithStatsViewModel.class);

        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory settingsFactory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, settingsFactory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();

        setupUI();
        setupObservers();

        viewModel.loadProfessorsWithStats();
    }

    private void setupUI() {
        recyclerView = findViewById(R.id.recyclerViewProfessors);
        progressBar = findViewById(R.id.progressBar);
        textSummary = findViewById(R.id.textSummary);
        textPageInfo = findViewById(R.id.textPageInfo);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        layoutPagination = findViewById(R.id.layoutPagination);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaginatedProfessorAdapter();
        recyclerView.setAdapter(adapter);

        setupLanguageToggle();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            viewModel.loadProfessorsWithStats();
        });

        btnPrev.setOnClickListener(v -> {
            adapter.prevPage();
            updatePaginationButtons();
        });

        btnNext.setOnClickListener(v -> {
            adapter.nextPage();
            updatePaginationButtons();
        });

        adapter.setOnPaginationChangeListener(new PaginatedProfessorAdapter.OnPaginationChangeListener() {
            @Override
            public void onPaginationChanged(int currentPage, int totalPages, int totalProfessors) {
                textPageInfo.setText(String.format("Page %d/%d", currentPage, totalPages));
                updatePaginationButtons();
            }
        });
    }

    private void updatePaginationButtons() {
        btnPrev.setEnabled(adapter.hasPrevPage());
        btnNext.setEnabled(adapter.hasNextPage());

        btnPrev.setTextColor(adapter.hasPrevPage() ? getResources().getColor(R.color.primary) : getResources().getColor(R.color.gray));
        btnNext.setTextColor(adapter.hasNextPage() ? getResources().getColor(R.color.primary) : getResources().getColor(R.color.gray));
    }

    private void setupLanguageToggle() {
        View langFr = findViewById(R.id.langFr);
        View langEn = findViewById(R.id.langEn);

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

    private void setupObservers() {
        viewModel.getProfessors().observe(this, professors -> {
            progressBar.setVisibility(View.GONE);
            if (professors != null && !professors.isEmpty()) {
                adapter.setAllProfessors(professors);
                recyclerView.setVisibility(View.VISIBLE);
                layoutPagination.setVisibility(View.VISIBLE);
                findViewById(R.id.textEmpty).setVisibility(View.GONE);

                updateSummary(professors);

                updatePaginationButtons();

            } else {
                recyclerView.setVisibility(View.GONE);
                layoutPagination.setVisibility(View.GONE);
                findViewById(R.id.textEmpty).setVisibility(View.VISIBLE);
                textSummary.setText("Aucun encadrant trouvé");
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary(List<ProfessorWithStats> professors) {
        int total = professors.size();
        int totalStudents = 0;
        double totalAverage = 0;
        int withAverage = 0;

        for (ProfessorWithStats professor : professors) {
            totalStudents += professor.getStudentCount();
            if (professor.getAverageScore() != null) {
                totalAverage += professor.getAverageScore();
                withAverage++;
            }
        }

        String summary = String.format("%d encadrants • %d étudiants • Moyenne: %.1f/20",
                total, totalStudents, withAverage > 0 ? totalAverage / withAverage : 0);
        textSummary.setText(summary);
    }
}