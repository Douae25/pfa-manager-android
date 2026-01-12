package ma.ensate.pfa_manager.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.DeliverableType;
import ma.ensate.pfa_manager.model.dto.DeliverableWithStudent;
import ma.ensate.pfa_manager.viewmodel.DeliverableListViewModel;

public class DeliverableListActivity extends AppCompatActivity
        implements DeliverableAdapter.OnDeliverableClickListener {

    private RecyclerView recyclerView;
    private FrameLayout loadingOverlay;
    private LinearLayout emptyStateLayout;
    private TextView tvDeliverableCount;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private TabLayout tabLayout;
    private ImageView btnBack;

    private DeliverableListViewModel viewModel;
    private DeliverableAdapter adapter;

    private Long currentSupervisorId;
    private List<DeliverableWithStudent> allItems = new ArrayList<>();
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

        setContentView(R.layout.activity_deliverable_list);

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
        tvDeliverableCount = findViewById(R.id.tvDeliverableCount);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        tabLayout = findViewById(R.id.tabLayout);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new DeliverableAdapter(this);
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
        viewModel = new ViewModelProvider(this).get(DeliverableListViewModel.class);
        viewModel.setSupervisorId(currentSupervisorId);
    }

    private void observeData() {
        viewModel.getDeliverablesWithStudents().observe(this, deliverables -> {
            hideLoading();
            if (deliverables != null) {
                allItems = deliverables;
                applyFilter();
            } else {
                showEmptyState("Aucun livrable", "Vos étudiants n'ont pas encore soumis de documents");
            }
        });

        viewModel.getDeliverableCount().observe(this, count -> {
            tvDeliverableCount.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        List<DeliverableWithStudent> filtered = new ArrayList<>();

        for (DeliverableWithStudent item : allItems) {
            switch (currentFilter) {
                case 0:
                    filtered.add(item);
                    break;
                case 1:
                    if (item.isBeforeDefense()) {
                        filtered.add(item);
                    }
                    break;
                case 2:
                    if (item.isAfterDefense()) {
                        filtered.add(item);
                    }
                    break;
            }
        }

        if (filtered.isEmpty()) {
            String title, subtitle;
            switch (currentFilter) {
                case 1:
                    title = "Aucun livrable avant soutenance";
                    subtitle = "Les rapports d'avancement et présentations apparaîtront ici";
                    break;
                case 2:
                    title = "Aucun livrable après soutenance";
                    subtitle = "Les rapports finaux apparaîtront ici";
                    break;
                default:
                    title = "Aucun livrable";
                    subtitle = "Vos étudiants n'ont pas encore soumis de documents";
            }
            showEmptyState(title, subtitle);
        } else {
            showList();
            adapter.submitList(new ArrayList<>(filtered));
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
    public void onOpenClick(DeliverableWithStudent item) {
        if (item == null || item.getFileUri() == null || item.getFileUri().isEmpty()) {
            Toast.makeText(this, "Fichier non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        showFileInfoDialog(item);
    }

    private void showFileInfoDialog(DeliverableWithStudent item) {
        File file = new File(item.getFileUri());
        boolean fileExists = file.exists();

        String typeLabel = "";
        if (item.getDeliverableType() != null) {
            typeLabel = item.isBeforeDefense() ? "Avant soutenance" : "Après soutenance";
        }

        String fileTypeLabel = "";
        if (item.getDeliverableFileType() != null) {
            switch (item.getDeliverableFileType()) {
                case RAPPORT_AVANCEMENT:
                    fileTypeLabel = "Rapport d'avancement";
                    break;
                case PRESENTATION:
                    fileTypeLabel = "Présentation";
                    break;
                case RAPPORT_FINAL:
                    fileTypeLabel = "Rapport final";
                    break;
            }
        }

        StringBuilder message = new StringBuilder();
        message.append("Fichier: ").append(item.getFileTitle()).append("\n\n");
        message.append("Étudiant: ").append(item.getStudentFullName()).append("\n");
        message.append("Projet: ").append(item.getPfaTitle()).append("\n");

        if (!typeLabel.isEmpty()) {
            message.append("Type: ").append(typeLabel).append("\n");
        }
        if (!fileTypeLabel.isEmpty()) {
            message.append("Catégorie: ").append(fileTypeLabel).append("\n");
        }

        message.append("\n");

        if (fileExists) {
            message.append("Taille: ").append(formatFileSize(file.length()));
        } else {
            message.append("⚠️ Le fichier n'existe pas sur le stockage.");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Détails du livrable");
        builder.setMessage(message.toString());

        if (fileExists) {
            builder.setPositiveButton("Ouvrir", (dialog, which) -> {
                openFile(item.getFileUri(), item.getFileTitle());
            });
        }

        builder.setNegativeButton("Fermer", null);
        builder.show();
    }

    private void openFile(String fileUri, String title) {
        if (fileUri == null || fileUri.isEmpty()) {
            Toast.makeText(this, "Fichier non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri;
            String mimeType = getMimeType(fileUri);

            if (fileUri.startsWith("http://") || fileUri.startsWith("https://")) {
                // URL DISTANTE
                uri = Uri.parse(fileUri);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(Intent.createChooser(intent, "Ouvrir " + title));

            } else {
                // FICHIER LOCAL
                File file = new File(fileUri);

                if (!file.exists()) {
                    Toast.makeText(this, "Fichier introuvable", Toast.LENGTH_LONG).show();
                    return;
                }

                uri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(intent, "Ouvrir " + title));
            }

        } catch (ActivityNotFoundException e) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUri));
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Aucune application pour ouvrir ce fichier", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getMimeType(String fileUri) {
        String lowerUri = fileUri.toLowerCase();
        if (lowerUri.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerUri.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerUri.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerUri.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerUri.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (lowerUri.endsWith(".jpg") || lowerUri.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUri.endsWith(".png")) {
            return "image/png";
        }
        return "*/*";
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }
}