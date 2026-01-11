package ma.ensate.pfa_manager.view;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.*;
import ma.ensate.pfa_manager.viewmodel.StudentDetailViewModel;

public class StudentDetailActivity extends AppCompatActivity {

    // Views - Section Étudiant
    private TextView tvAvatar;
    private TextView tvStudentName;
    private TextView tvStudentEmail;
    private TextView tvStudentPhone;

    // Views - Section Projet
    private TextView tvPfaTitle;
    private TextView tvPfaDescription;
    private TextView tvPfaStatus;

    // Views - Section Convention
    private CardView cardConvention;
    private LinearLayout layoutConventionEmpty;
    private LinearLayout layoutConventionDetails;
    private TextView tvCompanyName;
    private TextView tvCompanyAddress;
    private TextView tvConventionStatus;

    // Views - Section Livrables
    private TextView tvDeliverablesCount;
    private RecyclerView recyclerDeliverables;
    private TextView tvDeliverablesEmpty;

    // Views - Section Soutenance
    private CardView cardSoutenance;
    private LinearLayout layoutSoutenanceEmpty;
    private LinearLayout layoutSoutenanceDetails;
    private TextView tvSoutenanceDate;
    private TextView tvSoutenanceLocation;
    private TextView tvSoutenanceStatus;

    // ViewModel
    private StudentDetailViewModel viewModel;

    // Données
    private Long studentId;
    private Long pfaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        // Récupérer l'ID de l'étudiant depuis l'Intent
        studentId = getIntent().getLongExtra("STUDENT_ID", -1L);
        if (studentId == -1L) {
            Toast.makeText(this, "Erreur : Étudiant introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        initViewModel();
        observeData();
    }

    private void initViews() {
        // Section Étudiant
        tvAvatar = findViewById(R.id.tvAvatar);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentEmail = findViewById(R.id.tvStudentEmail);
        tvStudentPhone = findViewById(R.id.tvStudentPhone);

        // Section Projet
        tvPfaTitle = findViewById(R.id.tvPfaTitle);
        tvPfaDescription = findViewById(R.id.tvPfaDescription);
        tvPfaStatus = findViewById(R.id.tvPfaStatus);

        // Section Convention
        cardConvention = findViewById(R.id.cardConvention);
        layoutConventionEmpty = findViewById(R.id.layoutConventionEmpty);
        layoutConventionDetails = findViewById(R.id.layoutConventionDetails);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        tvConventionStatus = findViewById(R.id.tvConventionStatus);

        // Section Livrables
        tvDeliverablesCount = findViewById(R.id.tvDeliverablesCount);
        recyclerDeliverables = findViewById(R.id.recyclerDeliverables);
        tvDeliverablesEmpty = findViewById(R.id.tvDeliverablesEmpty);

        recyclerDeliverables.setLayoutManager(new LinearLayoutManager(this));

        // Section Soutenance
        cardSoutenance = findViewById(R.id.cardSoutenance);
        layoutSoutenanceEmpty = findViewById(R.id.layoutSoutenanceEmpty);
        layoutSoutenanceDetails = findViewById(R.id.layoutSoutenanceDetails);
        tvSoutenanceDate = findViewById(R.id.tvSoutenanceDate);
        tvSoutenanceLocation = findViewById(R.id.tvSoutenanceLocation);
        tvSoutenanceStatus = findViewById(R.id.tvSoutenanceStatus);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(StudentDetailViewModel.class);
        viewModel.setStudentId(studentId);
    }

    private void observeData() {
        // Observer l'étudiant
        viewModel.getStudent().observe(this, this::updateStudentInfo);

        // Observer le PFA
        viewModel.getStudentPFAs().observe(this, pfas -> {
            if (pfas != null && !pfas.isEmpty()) {
                PFADossier pfa = pfas.get(0); // Premier PFA
                pfaId = pfa.getPfa_id();
                viewModel.setPfaId(pfaId);
                updatePFAInfo(pfa);
            }
        });

        // Observer la convention
        viewModel.getConvention().observe(this, this::updateConventionInfo);

        // Observer les livrables
        viewModel.getDeliverables().observe(this, deliverables -> {
            if (deliverables != null && !deliverables.isEmpty()) {
                updateDeliverablesInfo(deliverables.size());
                // TODO: Créer DeliverableAdapter et l'afficher
                tvDeliverablesEmpty.setVisibility(View.GONE);
                recyclerDeliverables.setVisibility(View.VISIBLE);
            } else {
                tvDeliverablesCount.setText("0");
                tvDeliverablesEmpty.setVisibility(View.VISIBLE);
                recyclerDeliverables.setVisibility(View.GONE);
            }
        });

        // Observer la soutenance
        viewModel.getSoutenance().observe(this, this::updateSoutenanceInfo);
    }

    private void updateStudentInfo(User student) {
        if (student != null) {
            // Nom complet
            String fullName = student.getFirst_name() + " " + student.getLast_name();
            tvStudentName.setText(fullName);

            // Avatar avec initiales
            String initials = student.getFirst_name().substring(0, 1) +
                    student.getLast_name().substring(0, 1);
            tvAvatar.setText(initials.toUpperCase());

            // Email
            tvStudentEmail.setText(student.getEmail());

            // Téléphone
            if (student.getPhone_number() != null && !student.getPhone_number().isEmpty()) {
                tvStudentPhone.setText(student.getPhone_number());
                tvStudentPhone.setVisibility(View.VISIBLE);
            } else {
                tvStudentPhone.setVisibility(View.GONE);
            }
        }
    }

    private void updatePFAInfo(PFADossier pfa) {
        if (pfa != null) {
            tvPfaTitle.setText(pfa.getTitle());
            tvPfaDescription.setText(pfa.getDescription());

            // Status avec style
            PFAStatus status = pfa.getCurrent_status();
            setStatusStyle(tvPfaStatus, status);
        }
    }

    private void setStatusStyle(TextView textView, PFAStatus status) {
        switch (status) {
            case ASSIGNED:
                textView.setText("📋 Assigné");
                textView.setBackgroundResource(R.drawable.badge_status_assigned);
                break;
            case CONVENTION_PENDING:
                textView.setText("⏳ Convention en attente");
                textView.setBackgroundResource(R.drawable.badge_status_pending);
                break;
            case IN_PROGRESS:
                textView.setText("🔄 En cours");
                textView.setBackgroundResource(R.drawable.badge_status_in_progress);
                break;
            case CLOSED:
                textView.setText("✅ Clôturé");
                textView.setBackgroundResource(R.drawable.badge_status_closed);
                break;
        }
    }

    private void updateConventionInfo(Convention convention) {
        if (convention != null) {
            layoutConventionEmpty.setVisibility(View.GONE);
            layoutConventionDetails.setVisibility(View.VISIBLE);

            tvCompanyName.setText(convention.getCompany_name());
            tvCompanyAddress.setText(convention.getCompany_address());

            // Status de la convention
            ConventionState state = convention.getState();
            switch (state) {
                case GENERATED:
                    tvConventionStatus.setText("📄 Générée");
                    tvConventionStatus.setTextColor(getColor(android.R.color.holo_blue_dark));
                    break;
                case UPLOADED:
                    tvConventionStatus.setText("⏳ En attente de validation");
                    tvConventionStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
                    break;
                case VALIDATED:
                    tvConventionStatus.setText("✅ Validée");
                    tvConventionStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                    break;
                case REJECTED:
                    tvConventionStatus.setText("❌ Rejetée");
                    tvConventionStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                    break;
            }
        } else {
            layoutConventionEmpty.setVisibility(View.VISIBLE);
            layoutConventionDetails.setVisibility(View.GONE);
        }
    }

    private void updateDeliverablesInfo(int count) {
        tvDeliverablesCount.setText(String.valueOf(count));
    }

    private void updateSoutenanceInfo(Soutenance soutenance) {
        if (soutenance != null) {
            layoutSoutenanceEmpty.setVisibility(View.GONE);
            layoutSoutenanceDetails.setVisibility(View.VISIBLE);

            // Date formatée
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
            String dateStr = sdf.format(new Date(soutenance.getDate_soutenance()));
            tvSoutenanceDate.setText("📆 " + dateStr);

            // Lieu
            tvSoutenanceLocation.setText("📍 " + soutenance.getLocation());

            // Status
            if (soutenance.getStatus() == SoutenanceStatus.PLANNED) {
                tvSoutenanceStatus.setText("⏳ Planifiée");
                tvSoutenanceStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
            } else {
                tvSoutenanceStatus.setText("✅ Effectuée");
                tvSoutenanceStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            }
        } else {
            layoutSoutenanceEmpty.setVisibility(View.VISIBLE);
            layoutSoutenanceDetails.setVisibility(View.GONE);
        }
    }
}