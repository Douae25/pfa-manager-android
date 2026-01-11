package ma.ensate.pfa_manager.view;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.EvaluationCriteria;
import ma.ensate.pfa_manager.model.dto.SoutenanceWithEvaluation;
import ma.ensate.pfa_manager.viewmodel.EvaluationListViewModel;

public class EvaluationListActivity extends AppCompatActivity
        implements EvaluationAdapter.OnEvaluationClickListener {

    private RecyclerView recyclerView;
    private FrameLayout loadingOverlay;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private TabLayout tabLayout;
    private ImageView btnBack;

    private EvaluationListViewModel viewModel;
    private EvaluationAdapter adapter;

    private Long currentSupervisorId;
    private List<SoutenanceWithEvaluation> allItems = new ArrayList<>();
    private List<EvaluationCriteria> criteriaList = new ArrayList<>();
    private int currentFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentSupervisorId = getIntent().getLongExtra("USER_ID", -1L);

        if (currentSupervisorId == -1L) {
            Toast.makeText(this, "Erreur d'identifiant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_evaluation_list);

        initViews();
        setupRecyclerView();
        setupTabs();
        initViewModel();
        observeData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        tabLayout = findViewById(R.id.tabLayout);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new EvaluationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getPosition();
                applyFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(EvaluationListViewModel.class);
        viewModel.setSupervisorId(currentSupervisorId);
    }

    private void observeData() {
        viewModel.getSoutenancesWithEvaluations().observe(this, items -> {
            hideLoading();
            if (items != null) {
                allItems = items;
                applyFilter();
            } else {
                showEmptyState("Aucune soutenance", "Planifiez d'abord des soutenances");
            }
        });

        viewModel.getActiveCriteria().observe(this, criteria -> {
            if (criteria != null) {
                criteriaList = criteria;
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        viewModel.getToastMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.clearToast();
            }
        });
    }

    private void applyFilter() {
        List<SoutenanceWithEvaluation> filtered = new ArrayList<>();

        for (SoutenanceWithEvaluation item : allItems) {
            switch (currentFilter) {
                case 0:
                    filtered.add(item);
                    break;
                case 1:
                    if (!item.isEvaluated()) {
                        filtered.add(item);
                    }
                    break;
                case 2:
                    if (item.isEvaluated()) {
                        filtered.add(item);
                    }
                    break;
            }
        }

        if (filtered.isEmpty()) {
            String title, subtitle;
            switch (currentFilter) {
                case 1:
                    title = "Tout est évalué !";
                    subtitle = "Vous avez évalué toutes les soutenances";
                    break;
                case 2:
                    title = "Aucune évaluation";
                    subtitle = "Vous n'avez pas encore évalué de soutenance";
                    break;
                default:
                    title = "Aucune soutenance";
                    subtitle = "Planifiez d'abord des soutenances";
            }
            showEmptyState(title, subtitle);
        } else {
            showList();
            adapter.submitList(filtered);
        }
    }

    private void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    private void showList() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(String title, String subtitle) {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(subtitle);
    }

    @Override
    public void onEvaluateClick(SoutenanceWithEvaluation item) {
        if (criteriaList.isEmpty()) {
            Toast.makeText(this, "Aucun critère d'évaluation configuré", Toast.LENGTH_SHORT).show();
            return;
        }

        EvaluationFormDialog dialog = EvaluationFormDialog.newInstance(
                item.getPfaId(),
                item.getPfaTitle(),
                item.getStudentFullName()
        );

        dialog.setCriteriaList(criteriaList);

        dialog.setOnSaveListener((pfaId, criteriaScores) -> {
            viewModel.saveEvaluation(pfaId, criteriaScores);
        });

        dialog.show(getSupportFragmentManager(), "evaluation_form");
    }
}