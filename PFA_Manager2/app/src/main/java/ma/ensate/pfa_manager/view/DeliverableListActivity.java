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

import java.io.File;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.dto.DeliverableWithStudent;
import ma.ensate.pfa_manager.viewmodel.DeliverableListViewModel;

public class DeliverableListActivity extends AppCompatActivity
        implements DeliverableAdapter.OnDeliverableClickListener {

    private RecyclerView recyclerView;
    private FrameLayout loadingOverlay;
    private LinearLayout emptyStateLayout;
    private TextView tvDeliverableCount;
    private ImageView btnBack;

    private DeliverableListViewModel viewModel;
    private DeliverableAdapter adapter;

    private Long currentSupervisorId;

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
        initViewModel();
        observeData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvDeliverableCount = findViewById(R.id.tvDeliverableCount);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new DeliverableAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DeliverableListViewModel.class);
        viewModel.setSupervisorId(currentSupervisorId);
    }

    private void observeData() {
        viewModel.getDeliverablesWithStudents().observe(this, deliverables -> {
            hideLoading();
            if (deliverables != null && !deliverables.isEmpty()) {
                adapter.submitList(deliverables);
                showList();
            } else {
                showEmptyState();
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

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
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

        String message;
        if (fileExists) {
            message = "Fichier: " + item.getFileTitle() + "\n\n" +
                    "Étudiant: " + item.getStudentFullName() + "\n" +
                    "Projet: " + item.getPfaTitle() + "\n\n" +
                    "Chemin: " + item.getFileUri() + "\n" +
                    "Taille: " + formatFileSize(file.length());
        } else {
            message = "Fichier: " + item.getFileTitle() + "\n\n" +
                    "Étudiant: " + item.getStudentFullName() + "\n" +
                    "Projet: " + item.getPfaTitle() + "\n\n" +
                    "⚠️ Le fichier n'existe pas sur le stockage.\n" +
                    "Chemin attendu: " + item.getFileUri();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Détails du livrable");
        builder.setMessage(message);

        if (fileExists) {
            builder.setPositiveButton("Ouvrir", (dialog, which) -> {
                openFile(item.getFileUri(), item.getFileTitle());
            });
        }

        builder.setNegativeButton("Fermer", null);
        builder.show();
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
            Toast.makeText(this, "Aucune application pour ouvrir ce type de fichier", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Erreur de chemin: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        } else if (lowerUri.endsWith(".jpg") || lowerUri.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUri.endsWith(".png")) {
            return "image/png";
        } else if (lowerUri.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerUri.endsWith(".ppt") || lowerUri.endsWith(".pptx")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerUri.endsWith(".xls") || lowerUri.endsWith(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (lowerUri.endsWith(".zip")) {
            return "application/zip";
        }
        return "*/*";
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }
}