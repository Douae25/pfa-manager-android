package ma.ensate.pfa_manager.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.*;
import ma.ensate.pfa_manager.viewmodel.StudentDetailViewModel;

public class StudentDetailActivity extends AppCompatActivity
        implements SimpleDeliverableAdapter.OnDeliverableClickListener {

    private TextView tvAvatar;
    private TextView tvStudentName;
    private TextView tvStudentEmail;
    private TextView tvStudentPhone;

    private TextView tvPfaTitle;
    private TextView tvPfaDescription;
    private Chip chipPfaStatus;

    private MaterialCardView cardConvention;
    private LinearLayout layoutConventionEmpty;
    private LinearLayout layoutConventionDetails;
    private TextView tvCompanyName;
    private TextView tvCompanyAddress;
    private Chip chipConventionStatus;
    private MaterialButton btnOpenConvention;

    private TextView tvDeliverablesCount;
    private RecyclerView recyclerDeliverables;
    private TextView tvDeliverablesEmpty;

    private MaterialCardView cardSoutenance;
    private LinearLayout layoutSoutenanceEmpty;
    private LinearLayout layoutSoutenanceDetails;
    private TextView tvSoutenanceDate;
    private TextView tvSoutenanceLocation;
    private Chip chipSoutenanceStatus;

    private StudentDetailViewModel viewModel;
    private SimpleDeliverableAdapter deliverableAdapter;

    private Long studentId;
    private Long pfaId;
    private Convention currentConvention;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        studentId = getIntent().getLongExtra("STUDENT_ID", -1L);
        if (studentId == -1L) {
            Toast.makeText(this, "Erreur : Étudiant introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupDeliverableAdapter();
        initViewModel();
        observeData();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentEmail = findViewById(R.id.tvStudentEmail);
        tvStudentPhone = findViewById(R.id.tvStudentPhone);

        tvPfaTitle = findViewById(R.id.tvPfaTitle);
        tvPfaDescription = findViewById(R.id.tvPfaDescription);
        chipPfaStatus = findViewById(R.id.tvPfaStatus);

        cardConvention = findViewById(R.id.cardConvention);
        layoutConventionEmpty = findViewById(R.id.layoutConventionEmpty);
        layoutConventionDetails = findViewById(R.id.layoutConventionDetails);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        chipConventionStatus = findViewById(R.id.tvConventionStatus);
        btnOpenConvention = findViewById(R.id.btnOpenConvention);

        tvDeliverablesCount = findViewById(R.id.tvDeliverablesCount);
        recyclerDeliverables = findViewById(R.id.recyclerDeliverables);
        tvDeliverablesEmpty = findViewById(R.id.tvDeliverablesEmpty);

        cardSoutenance = findViewById(R.id.cardSoutenance);
        layoutSoutenanceEmpty = findViewById(R.id.layoutSoutenanceEmpty);
        layoutSoutenanceDetails = findViewById(R.id.layoutSoutenanceDetails);
        tvSoutenanceDate = findViewById(R.id.tvSoutenanceDate);
        tvSoutenanceLocation = findViewById(R.id.tvSoutenanceLocation);
        chipSoutenanceStatus = findViewById(R.id.tvSoutenanceStatus);
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

    private void setupDeliverableAdapter() {
        deliverableAdapter = new SimpleDeliverableAdapter();
        deliverableAdapter.setOnDeliverableClickListener(this);
        recyclerDeliverables.setLayoutManager(new LinearLayoutManager(this));
        recyclerDeliverables.setAdapter(deliverableAdapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(StudentDetailViewModel.class);
        viewModel.setStudentId(studentId);
    }

    private void observeData() {
        viewModel.getStudent().observe(this, this::updateStudentInfo);

        viewModel.getStudentPFAs().observe(this, pfas -> {
            if (pfas != null && !pfas.isEmpty()) {
                PFADossier pfa = pfas.get(0);
                pfaId = pfa.getPfa_id();
                viewModel.setPfaId(pfaId);
                updatePFAInfo(pfa);
            }
        });

        viewModel.getConvention().observe(this, this::updateConventionInfo);

        viewModel.getDeliverables().observe(this, deliverables -> {
            if (deliverables != null && !deliverables.isEmpty()) {
                tvDeliverablesCount.setText(String.valueOf(deliverables.size()));
                deliverableAdapter.setDeliverables(deliverables);
                tvDeliverablesEmpty.setVisibility(View.GONE);
                recyclerDeliverables.setVisibility(View.VISIBLE);
            } else {
                tvDeliverablesCount.setText("0");
                tvDeliverablesEmpty.setVisibility(View.VISIBLE);
                recyclerDeliverables.setVisibility(View.GONE);
            }
        });

        viewModel.getSoutenance().observe(this, this::updateSoutenanceInfo);

        viewModel.getEvaluation().observe(this, this::updateEvaluationInfo);
    }

    private void updateStudentInfo(User student) {
        if (student != null) {
            String fullName = student.getFirst_name() + " " + student.getLast_name();
            tvStudentName.setText(fullName);

            String initials = student.getFirst_name().substring(0, 1) +
                    student.getLast_name().substring(0, 1);
            tvAvatar.setText(initials.toUpperCase());

            tvStudentEmail.setText(student.getEmail());

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

            PFAStatus status = pfa.getCurrent_status();
            setStatusChip(chipPfaStatus, status);
        }
    }

    private void setStatusChip(Chip chip, PFAStatus status) {
        switch (status) {
            case ASSIGNED:
                chip.setText("Assigné");
                chip.setChipBackgroundColorResource(R.color.primary_light);
                chip.setTextColor(getColor(R.color.primary));
                break;
            case CONVENTION_PENDING:
                chip.setText("Convention en attente");
                chip.setChipBackgroundColorResource(R.color.status_pending);
                chip.setTextColor(getColor(R.color.text_primary));
                break;
            case IN_PROGRESS:
                chip.setText("En cours");
                chip.setChipBackgroundColorResource(R.color.status_planned);
                chip.setTextColor(getColor(R.color.white));
                break;
            case CLOSED:
                chip.setText("Terminé");
                chip.setChipBackgroundColorResource(R.color.status_planned);
                chip.setTextColor(getColor(R.color.white));
                break;
        }
    }

    private void updateConventionInfo(Convention convention) {
        this.currentConvention = convention;

        if (convention != null) {
            layoutConventionEmpty.setVisibility(View.GONE);
            layoutConventionDetails.setVisibility(View.VISIBLE);

            tvCompanyName.setText(convention.getCompany_name());
            tvCompanyAddress.setText(convention.getCompany_address());

            ConventionState state = convention.getState();
            switch (state) {
                case GENERATED:
                    chipConventionStatus.setText("Générée");
                    chipConventionStatus.setChipBackgroundColorResource(R.color.primary_light);
                    chipConventionStatus.setTextColor(getColor(R.color.primary));
                    break;
                case UPLOADED:
                    chipConventionStatus.setText("En attente");
                    chipConventionStatus.setChipBackgroundColorResource(R.color.status_pending);
                    chipConventionStatus.setTextColor(getColor(R.color.text_primary));
                    break;
                case VALIDATED:
                    chipConventionStatus.setText("Validée");
                    chipConventionStatus.setChipBackgroundColorResource(R.color.status_planned);
                    chipConventionStatus.setTextColor(getColor(R.color.white));
                    break;
                case REJECTED:
                    chipConventionStatus.setText("Rejetée");
                    chipConventionStatus.setChipBackgroundColorResource(R.color.error);
                    chipConventionStatus.setTextColor(getColor(R.color.white));
                    break;
            }

            if (convention.getScanned_file_uri() != null && !convention.getScanned_file_uri().isEmpty()) {
                btnOpenConvention.setVisibility(View.VISIBLE);
                btnOpenConvention.setOnClickListener(v -> openConventionPdf());
            } else {
                btnOpenConvention.setVisibility(View.GONE);
            }
        } else {
            layoutConventionEmpty.setVisibility(View.VISIBLE);
            layoutConventionDetails.setVisibility(View.GONE);
        }
    }

    private void openConventionPdf() {
        if (currentConvention == null || currentConvention.getScanned_file_uri() == null) {
            Toast.makeText(this, "Aucun fichier de convention", Toast.LENGTH_SHORT).show();
            return;
        }

        openFile(currentConvention.getScanned_file_uri(), "Convention");
    }

    private void updateSoutenanceInfo(Soutenance soutenance) {
        if (soutenance != null) {
            layoutSoutenanceEmpty.setVisibility(View.GONE);
            layoutSoutenanceDetails.setVisibility(View.VISIBLE);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
            String dateStr = sdf.format(new Date(soutenance.getDate_soutenance()));
            tvSoutenanceDate.setText(dateStr);

            tvSoutenanceLocation.setText(soutenance.getLocation());

            if (soutenance.getStatus() == SoutenanceStatus.PLANNED) {
                chipSoutenanceStatus.setText("Planifiée");
                chipSoutenanceStatus.setChipBackgroundColorResource(R.color.status_pending);
                chipSoutenanceStatus.setTextColor(getColor(R.color.text_primary));
            } else {
                chipSoutenanceStatus.setText("Effectuée");
                chipSoutenanceStatus.setChipBackgroundColorResource(R.color.status_planned);
                chipSoutenanceStatus.setTextColor(getColor(R.color.white));
            }
        } else {
            layoutSoutenanceEmpty.setVisibility(View.VISIBLE);
            layoutSoutenanceDetails.setVisibility(View.GONE);
        }
    }

    private void updateEvaluationInfo(Evaluation evaluation) {
        if (evaluation != null && evaluation.getTotal_score() != null) {
            chipPfaStatus.setText(String.format(Locale.FRENCH, "Note: %.1f/20", evaluation.getTotal_score()));
            chipPfaStatus.setChipBackgroundColorResource(R.color.status_planned);
            chipPfaStatus.setTextColor(getColor(R.color.white));
        }
    }

    @Override
    public void onDeliverableClick(Deliverable deliverable) {
        if (deliverable == null || deliverable.getFile_uri() == null) {
            Toast.makeText(this, "Fichier non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        openFile(deliverable.getFile_uri(), deliverable.getFile_title());
    }

    private void openFile(String fileUri, String title) {
        try {
            File file = new File(fileUri);

            if (!file.exists()) {
                Toast.makeText(this, "Fichier introuvable: " + fileUri, Toast.LENGTH_LONG).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file);

            String mimeType = getMimeType(fileUri);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Ouvrir " + title));

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Aucune application pour ouvrir ce fichier", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getMimeType(String fileUri) {
        String lowerUri = fileUri.toLowerCase();
        if (lowerUri.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerUri.endsWith(".doc") || lowerUri.endsWith(".docx")) {
            return "application/msword";
        } else if (lowerUri.endsWith(".jpg") || lowerUri.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUri.endsWith(".png")) {
            return "image/png";
        }
        return "*/*";
    }
}