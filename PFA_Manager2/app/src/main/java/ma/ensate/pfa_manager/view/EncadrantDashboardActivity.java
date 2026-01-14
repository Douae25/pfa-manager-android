package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.viewmodel.EncadrantDashboardViewModel;

public class EncadrantDashboardActivity extends AppCompatActivity {

    private TextView tvProfName;
    private TextView tvStudentCount;
    private TextView tvUnplannedSoutenances;
    private TextView tvUnevaluatedStudents;

    private CardView cardStudents;
    private CardView cardValidations;
    private CardView cardEvaluations;
    private CardView cardPlanning;

    private ImageView btnLogout;
    private TextView badgeValidations;
    private TextView badgePlanning;

    private EncadrantDashboardViewModel viewModel;
    private Long currentSupervisorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentSupervisorId = getIntent().getLongExtra("USER_ID", -1L);
        if (currentSupervisorId == -1L) {
            Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_encadrant_dashboard);

        initViews();
        initViewModel();
        observeData();
        setupClickListeners();
    }

    private void initViews() {
        tvProfName = findViewById(R.id.tvProfName);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvUnplannedSoutenances = findViewById(R.id.tvUnplannedSoutenances);
        tvUnevaluatedStudents = findViewById(R.id.tvUnevaluatedStudents);

        cardStudents = findViewById(R.id.cardStudents);
        cardValidations = findViewById(R.id.cardValidations);
        cardEvaluations = findViewById(R.id.cardEvaluations);
        cardPlanning = findViewById(R.id.cardPlanning);

        btnLogout = findViewById(R.id.btnLogout);
        badgeValidations = findViewById(R.id.badgeValidations);
        badgePlanning = findViewById(R.id.badgePlanning);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(EncadrantDashboardViewModel.class);
        viewModel.setSupervisorId(currentSupervisorId);
    }

    private void observeData() {
        viewModel.getCurrentUser().observe(this, this::updateUserInfo);

        viewModel.getMyStudents().observe(this, students -> {
            int count = students != null ? students.size() : 0;
            tvStudentCount.setText(String.valueOf(count));
        });

        viewModel.getUnplannedSoutenancesCount().observe(this, count -> {
            tvUnplannedSoutenances.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getUnevaluatedStudentsCount().observe(this, count -> {
            tvUnevaluatedStudents.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getDeliverablesCount().observe(this, count -> {
            updateBadge(badgeValidations, count != null ? count : 0);
        });

        viewModel.getSoutenancesCount().observe(this, count -> {
            updateBadge(badgePlanning, count != null ? count : 0);
        });
    }

    private void updateUserInfo(User user) {
        if (user != null) {
            String fullName = "Pr. " + user.getFirst_name() + " " + user.getLast_name();
            tvProfName.setText(fullName);
        } else {
            tvProfName.setText("Pr. Non connecté");
        }
    }

    private void updateBadge(TextView badge, int count) {
        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Bouton Logout - Retour vers HomeActivity
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardStudents.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentListActivity.class);
            intent.putExtra("USER_ID", currentSupervisorId);
            startActivity(intent);
        });

        cardValidations.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeliverableListActivity.class);
            intent.putExtra("USER_ID", currentSupervisorId);
            startActivity(intent);
        });

        cardEvaluations.setOnClickListener(v -> {
            Intent intent = new Intent(this, EvaluationListActivity.class);
            intent.putExtra("USER_ID", currentSupervisorId);
            startActivity(intent);
        });

        cardPlanning.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlanningSoutenanceActivity.class);
            intent.putExtra("USER_ID", currentSupervisorId);
            startActivity(intent);
        });
    }
}