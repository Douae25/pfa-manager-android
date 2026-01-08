package ma.ensate.pfa_manager.view.etudiant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.DeliverableFile;
import ma.ensate.pfa_manager.model.DeliverableType;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.DeliverableRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.view.etudiant.adapters.DeliverableFilesAdapter;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class UploadDeliverablesActivity extends AppCompatActivity {
    
    private static final int FILE_PICKER_REQUEST = 1001;
    
    private SettingsViewModel settingsViewModel;
    private User currentUser;
    private boolean isBeforeSoutenance;
    private DeliverableRepository deliverableRepository;
    private PFADossierRepository pfaDossierRepository;
    
    private Button btnAddFile, btnUploadAll;
    private TextView tvTitle;
    private RecyclerView recyclerViewFiles;
    private DeliverableFilesAdapter adapter;
    private List<DeliverableFile> selectedFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
        
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_deliverables);

        currentUser = (User) getIntent().getSerializableExtra("user");
        isBeforeSoutenance = getIntent().getBooleanExtra("before_soutenance", true);
        selectedFiles = new ArrayList<>();
        
        deliverableRepository = new DeliverableRepository(getApplication());
        pfaDossierRepository = new PFADossierRepository(getApplication());

        setupBackNavigation();
        initViews();
        setupTitle();
        setupRecyclerView();
        setupFileSelection();
        setupUploadButton();
    }

    private void setupBackNavigation() {
        ImageView backArrow = findViewById(R.id.backArrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        btnAddFile = findViewById(R.id.btnAddFile);
        btnUploadAll = findViewById(R.id.btnUploadAll);
        tvTitle = findViewById(R.id.tvTitle);
        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
    }

    private void setupTitle() {
        if (isBeforeSoutenance) {
            tvTitle.setText(R.string.upload_deliverables_before);
        } else {
            tvTitle.setText(R.string.upload_deliverables_after);
        }
    }

    private void setupRecyclerView() {
        adapter = new DeliverableFilesAdapter(selectedFiles, position -> {
            selectedFiles.remove(position);
            adapter.notifyItemRemoved(position);
            updateEmptyState();
        });
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFiles.setAdapter(adapter);
    }

    private void setupFileSelection() {
        btnAddFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_PICKER_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String fileName = fileUri.getLastPathSegment();
                // TODO: Get actual file size
                long fileSize = 0;
                
                DeliverableFile deliverableFile = new DeliverableFile(fileName, fileSize, fileUri.toString());
                selectedFiles.add(deliverableFile);
                adapter.notifyItemInserted(selectedFiles.size() - 1);
                updateEmptyState();
                
                Toast.makeText(this, R.string.file_added, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupUploadButton() {
        btnUploadAll.setOnClickListener(v -> uploadDeliverables());
    }

    private void uploadDeliverables() {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, R.string.error_no_files, Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer le dossier PFA de l'étudiant
        pfaDossierRepository.getByStudentId(currentUser.getUser_id(), pfaDossier -> {
            if (pfaDossier == null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, R.string.error_no_pfa_dossier, Toast.LENGTH_SHORT).show()
                );
                return;
            }
            
            // Sauvegarder chaque fichier dans le stockage et en BD
            new Thread(() -> {
                int successCount = 0;
                for (DeliverableFile file : selectedFiles) {
                    try {
                        // Copier le fichier dans le stockage app
                        String savedFilePath = copyFileToAppStorage(Uri.parse(file.getFileUri()), file.getFileName());
                        
                        if (savedFilePath != null) {
                            // Créer l'entrée en base de données
                            Deliverable deliverable = new Deliverable();
                            deliverable.setPfa_id(pfaDossier.getPfa_id());
                            deliverable.setFile_title(file.getFileName());
                            deliverable.setFile_uri(savedFilePath);
                            deliverable.setDeliverable_type(isBeforeSoutenance ? 
                                DeliverableType.BEFORE_DEFENSE : DeliverableType.AFTER_DEFENSE);
                            deliverable.setUploaded_at(System.currentTimeMillis());
                            
                            deliverableRepository.insert(deliverable, null);
                            successCount++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                final int finalSuccessCount = successCount;
                runOnUiThread(() -> {
                    if (finalSuccessCount > 0) {
                        Toast.makeText(this, 
                            getString(R.string.deliverables_uploaded_success) + " (" + finalSuccessCount + " fichier(s))", 
                            Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, R.string.error_upload_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }
    
    private String copyFileToAppStorage(Uri sourceUri, String fileName) throws Exception {
        File deliverablesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (deliverablesDir == null) {
            return null;
        }
        
        if (!deliverablesDir.exists()) {
            deliverablesDir.mkdirs();
        }
        
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = fileName.substring(dotIndex);
        }
        String newFileName = "Deliverable_" + timeStamp + fileExtension;
        File destFile = new File(deliverablesDir, newFileName);
        
        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destFile)) {
            
            if (inputStream == null) {
                return null;
            }
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            return destFile.getAbsolutePath();
        }
    }
    
    private void updateEmptyState() {
        if (selectedFiles.isEmpty()) {
            recyclerViewFiles.setVisibility(RecyclerView.GONE);
        } else {
            recyclerViewFiles.setVisibility(RecyclerView.VISIBLE);
        }
    }
}
