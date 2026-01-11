package ma.ensate.pfa_manager.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.PFAWithSoutenance;
import ma.ensate.pfa_manager.viewmodel.PlanningViewModel;

public class PlanningSoutenanceActivity extends AppCompatActivity
        implements PFASoutenanceAdapter.OnItemClickListener {

    private PlanningViewModel viewModel;
    private RecyclerView recyclerView;
    private PFASoutenanceAdapter adapter;
    private TabLayout tabLayout;
    private android.widget.LinearLayout tvEmptyState;
    private View loadingOverlay;

    private Long currentSupervisorId;
    private List<PFAWithSoutenance> allItems = new ArrayList<>();
    private int currentFilter = 0; // 0: All, 1: Non planifié, 2: Planifié

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_soutenance);

        currentSupervisorId = getIntent().getLongExtra("USER_ID", -1L);
        if (currentSupervisorId == -1) {
            Toast.makeText(this, "Erreur d'identification", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupViewModel();
    }

    private void initViews() {
        // Toolbar
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // TabLayout pour filtrer
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getPosition();
                filterItems();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PFASoutenanceAdapter(this);
        recyclerView.setAdapter(adapter);

        // Empty state
        tvEmptyState = findViewById(R.id.tvEmptyState);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(PlanningViewModel.class);

        viewModel.getPFAsWithSoutenances(currentSupervisorId).observe(this, items -> {
            loadingOverlay.setVisibility(View.GONE);
            allItems = items != null ? items : new ArrayList<>();
            updateTabBadges();
            filterItems();
        });

        viewModel.getStatusMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOperationSuccess().observe(this, success -> {
            if (success) {
                viewModel.resetOperationSuccess();
            }
        });
    }

    private void updateTabBadges() {
        long nonPlanifie = allItems.stream().filter(i -> !i.isPlanned()).count();
        long planifie = allItems.stream().filter(PFAWithSoutenance::isPlanned).count();

        tabLayout.getTabAt(0).setText("Tous (" + allItems.size() + ")");
        tabLayout.getTabAt(1).setText("Non planifié (" + nonPlanifie + ")");
        tabLayout.getTabAt(2).setText("Planifié (" + planifie + ")");
    }

    private void filterItems() {
        List<PFAWithSoutenance> filtered;

        switch (currentFilter) {
            case 1:
                filtered = allItems.stream()
                        .filter(i -> !i.isPlanned())
                        .collect(Collectors.toList());
                break;
            case 2:
                filtered = allItems.stream()
                        .filter(PFAWithSoutenance::isPlanned)
                        .collect(Collectors.toList());
                break;
            default:
                filtered = allItems;
        }

        adapter.submitList(filtered);
        tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onPlanClick(PFAWithSoutenance item) {
        SoutenanceFormDialog dialog = SoutenanceFormDialog.newInstance(
                item.pfa.getPfa_id(),
                item.pfa.getTitle(),
                null, null, null
        );
        dialog.setOnSaveListener((pfaId, lieu, date) -> {
            viewModel.planifierSoutenance(pfaId, lieu, date);
        });
        dialog.show(getSupportFragmentManager(), "plan_dialog");
    }

    @Override
    public void onEditClick(PFAWithSoutenance item) {
        SoutenanceFormDialog dialog = SoutenanceFormDialog.newInstance(
                item.pfa.getPfa_id(),
                item.pfa.getTitle(),
                item.soutenance.getSoutenance_id(),
                item.soutenance.getLocation(),
                item.soutenance.getDate_soutenance()
        );
        dialog.setOnSaveListener((pfaId, lieu, date) -> {
            item.soutenance.setLocation(lieu);
            item.soutenance.setDate_soutenance(date);
            viewModel.modifierSoutenance(item.soutenance, lieu, date);
        });
        dialog.show(getSupportFragmentManager(), "edit_dialog");
    }

    @Override
    public void onDeleteClick(PFAWithSoutenance item) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Supprimer la soutenance")
                .setMessage("Voulez-vous vraiment supprimer cette planification ?")
                .setPositiveButton("Supprimer", (d, w) -> {
                    viewModel.supprimerSoutenance(item.soutenance.getSoutenance_id());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}