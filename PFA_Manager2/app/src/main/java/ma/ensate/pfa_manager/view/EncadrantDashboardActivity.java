package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.viewmodel.EncadrantDashboardViewModel;

public class EncadrantDashboardActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private TextView tvProfName;
    private TextView tvStudentCount;
    private TextView tvPfaCount;
    private TextView tvTotalNotifications;

    // Cards
    private CardView cardStudents;
    private CardView cardValidations;
    private CardView cardEvaluations;
    private CardView cardPlanning;

    // Notifications
    private FrameLayout notificationContainer;
    private TextView badgeNotifications;
    private TextView badgeValidations;
    private TextView badgePlanning;

    // ViewModel
    private EncadrantDashboardViewModel viewModel;

    // ID du superviseur connecté
    private Long currentSupervisorId = 1L; // TODO: Remplacer par système d'auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encadrant_dashboard);

        initViews();
        initViewModel();
        observeData();
        setupClickListeners();
    }

    private void initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TextViews
        tvProfName = findViewById(R.id.tvProfName);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvPfaCount = findViewById(R.id.tvPfaCount);
        tvTotalNotifications = findViewById(R.id.tvTotalNotifications);

        // Cards
        cardStudents = findViewById(R.id.cardStudents);
        cardValidations = findViewById(R.id.cardValidations);
        cardEvaluations = findViewById(R.id.cardEvaluations);
        cardPlanning = findViewById(R.id.cardPlanning);

        // Notifications
        notificationContainer = findViewById(R.id.notificationContainer);
        badgeNotifications = findViewById(R.id.badgeNotifications);
        badgeValidations = findViewById(R.id.badgeValidations);
        badgePlanning = findViewById(R.id.badgePlanning);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(EncadrantDashboardViewModel.class);
        viewModel.setSupervisorId(currentSupervisorId);
    }

    private void observeData() {
        // Observer l'utilisateur connecté
        viewModel.getCurrentUser().observe(this, this::updateUserInfo);

        // Observer le nombre d'étudiants
        viewModel.getMyStudents().observe(this, students -> {
            int count = students != null ? students.size() : 0;
            tvStudentCount.setText(String.valueOf(count));
        });

        // Observer le nombre de PFAs
        viewModel.getPfaCount().observe(this, count -> {
            tvPfaCount.setText(String.valueOf(count != null ? count : 0));
        });

        // Observer le nombre de livrables à valider
        viewModel.getDeliverablesCount().observe(this, count -> {
            updateBadge(badgeValidations, count != null ? count : 0);
        });

        // Observer le nombre de soutenances planifiées
        viewModel.getSoutenancesCount().observe(this, count -> {
            updateBadge(badgePlanning, count != null ? count : 0);
        });

        // Observer le total de notifications
        viewModel.getTotalNotifications().observe(this, total -> {
            int notifCount = total != null ? total : 0;
            tvTotalNotifications.setText(String.valueOf(notifCount));
            updateBadge(badgeNotifications, notifCount);
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
        // Icône notifications (Toolbar)
        notificationContainer.setOnClickListener(v -> {
            showToast("Centre de notifications");
            // TODO: Intent vers NotificationsActivity
        });

        // Card Étudiants
        cardStudents.setOnClickListener(v -> {
            showToast("Liste des étudiants");
            // TODO: Intent vers StudentListActivity
        });

        // Card Validations
        cardValidations.setOnClickListener(v -> {
            showToast("Validations de livrables");
            // TODO: Intent vers ValidationListActivity
        });

        // Card Évaluations
        cardEvaluations.setOnClickListener(v -> {
            showToast("Module d'évaluation");
            // TODO: Intent vers EvaluationActivity
        });

        // Card Planification
        cardPlanning.setOnClickListener(v -> {
            showToast("Planification des soutenances");
            // TODO: Intent vers PlanningActivity
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}