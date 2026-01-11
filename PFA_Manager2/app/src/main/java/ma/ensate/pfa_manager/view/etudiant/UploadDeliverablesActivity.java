package ma.ensate.pfa_manager.view.etudiant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import ma.ensate.pfa_manager.view.etudiant.adapters.RapportAvancementAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.DeliverableType;
import ma.ensate.pfa_manager.model.DeliverableFileType;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.DeliverableRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class UploadDeliverablesActivity extends AppCompatActivity {
    
    private static final int FILE_PICKER_RAPPORT_AVANCEMENT = 1001;
    private static final int FILE_PICKER_PRESENTATION = 1002;
    private static final int FILE_PICKER_RAPPORT_FINAL = 1003;
    
    private SettingsViewModel settingsViewModel;
    private User currentUser;
    private boolean isBeforeSoutenance;
    private DeliverableRepository deliverableRepository;
    private PFADossierRepository pfaDossierRepository;
    
    private Button btnUploadAll;
    private TextView tvTitle;
    
    // Sections
    private LinearLayout sectionRapportAvancement, sectionPresentation, sectionRapportFinal;
    
    // Rapport d'Avancement (liste pour plusieurs fichiers)
    private Button btnAddRapportAvancement;
    private RecyclerView recyclerViewRapportAvancement;
    private RapportAvancementAdapter rapportAvancementAdapter;
    private List<String> rapportAvancementPaths = new ArrayList<>();
    private TextView tvEmptyRapportAvancement;
    
    // Présentation
    private Button btnAddPresentation, btnRemovePresentation;
    private TextView tvPresentationName;
    private LinearLayout presentationControls;
    private String presentationPath = null;
    
    // Rapport Final
    private Button btnAddRapportFinal, btnRemoveRapportFinal;
    private TextView tvRapportFinalName;
    private LinearLayout rapportFinalControls;
    private String rapportFinalPath = null;

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
        
        deliverableRepository = new DeliverableRepository(getApplication());
        pfaDossierRepository = new PFADossierRepository(getApplication());

        setupBackNavigation();
        initViews();
        setupTitle();
        setupSections();
        setupUploadButton();
    }

    private void setupBackNavigation() {
        ImageView backArrow = findViewById(R.id.backArrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        btnUploadAll = findViewById(R.id.btnUploadAll);
        tvTitle = findViewById(R.id.tvTitle);
        
        // Sections
        sectionRapportAvancement = findViewById(R.id.sectionRapportAvancement);
        sectionPresentation = findViewById(R.id.sectionPresentation);
        sectionRapportFinal = findViewById(R.id.sectionRapportFinal);
        
        // Rapport d'Avancement
        btnAddRapportAvancement = findViewById(R.id.btnAddRapportAvancement);
        recyclerViewRapportAvancement = findViewById(R.id.recyclerViewRapportAvancement);
        tvEmptyRapportAvancement = findViewById(R.id.tvEmptyRapportAvancement);
        
        // Présentation
        btnAddPresentation = findViewById(R.id.btnAddPresentation);
        btnRemovePresentation = findViewById(R.id.btnRemovePresentation);
        tvPresentationName = findViewById(R.id.tvPresentationName);
        presentationControls = findViewById(R.id.presentationControls);
        
        // Rapport Final
        btnAddRapportFinal = findViewById(R.id.btnAddRapportFinal);
        btnRemoveRapportFinal = findViewById(R.id.btnRemoveRapportFinal);
        tvRapportFinalName = findViewById(R.id.tvRapportFinalName);
        rapportFinalControls = findViewById(R.id.rapportFinalControls);
    }

    private void setupTitle() {
        if (isBeforeSoutenance) {
            tvTitle.setText(R.string.upload_deliverables_before);
        } else {
            tvTitle.setText(R.string.upload_deliverables_after);
        }
    }
    
    private void setupSections() {
        if (isBeforeSoutenance) {
            // Avant soutenance: afficher tous les trois types
            sectionRapportAvancement.setVisibility(LinearLayout.VISIBLE);
            sectionPresentation.setVisibility(LinearLayout.VISIBLE);
            sectionRapportFinal.setVisibility(LinearLayout.VISIBLE);
            
            setupRapportAvancementButtons();
            setupPresentationButtons();
            setupRapportFinalButtons();
        } else {
            // Après soutenance: masquer rapport d'avancement, afficher présentation et rapport final
            sectionRapportAvancement.setVisibility(LinearLayout.GONE);
            sectionPresentation.setVisibility(LinearLayout.VISIBLE);
            sectionRapportFinal.setVisibility(LinearLayout.VISIBLE);
            
            setupPresentationButtons();
            setupRapportFinalButtons();
        }
    }
    
    private void setupRapportAvancementButtons() {
        // Setup RecyclerView for rapport d'avancement
        rapportAvancementAdapter = new RapportAvancementAdapter(rapportAvancementPaths, position -> {
            rapportAvancementPaths.remove(position);
            rapportAvancementAdapter.notifyItemRemoved(position);
            updateRapportAvancementUI();
        });
        recyclerViewRapportAvancement.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRapportAvancement.setAdapter(rapportAvancementAdapter);
        
        // Add button
        btnAddRapportAvancement.setOnClickListener(v -> openFilePicker(FILE_PICKER_RAPPORT_AVANCEMENT));
        
        updateRapportAvancementUI();
    }
    
    private void updateRapportAvancementUI() {
        if (rapportAvancementPaths.isEmpty()) {
            recyclerViewRapportAvancement.setVisibility(RecyclerView.GONE);
            tvEmptyRapportAvancement.setVisibility(TextView.VISIBLE);
        } else {
            recyclerViewRapportAvancement.setVisibility(RecyclerView.VISIBLE);
            tvEmptyRapportAvancement.setVisibility(TextView.GONE);
        }
    }
    
    private void setupPresentationButtons() {
        btnAddPresentation.setOnClickListener(v -> openFilePicker(FILE_PICKER_PRESENTATION));
        btnRemovePresentation.setOnClickListener(v -> {
            presentationPath = null;
            updatePresentationUI();
        });
    }
    
    private void setupRapportFinalButtons() {
        btnAddRapportFinal.setOnClickListener(v -> openFilePicker(FILE_PICKER_RAPPORT_FINAL));
        btnRemoveRapportFinal.setOnClickListener(v -> {
            rapportFinalPath = null;
            updateRapportFinalUI();
        });
    }
    
    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                try {
                    String fileName = getFileName(fileUri);
                    String filePath = copyFileToAppStorage(fileUri, fileName);
                    
                    if (filePath != null) {
                        if (requestCode == FILE_PICKER_RAPPORT_AVANCEMENT) {
                            rapportAvancementPaths.add(filePath);
                            rapportAvancementAdapter.notifyItemInserted(rapportAvancementPaths.size() - 1);
                            updateRapportAvancementUI();
                            Toast.makeText(this, R.string.file_added, Toast.LENGTH_SHORT).show();
                        } else if (requestCode == FILE_PICKER_PRESENTATION) {
                            presentationPath = filePath;
                            updatePresentationUI();
                            Toast.makeText(this, R.string.file_added, Toast.LENGTH_SHORT).show();
                        } else if (requestCode == FILE_PICKER_RAPPORT_FINAL) {
                            rapportFinalPath = filePath;
                            updateRapportFinalUI();
                            Toast.makeText(this, R.string.file_added, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error_upload_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private String getFileName(Uri uri) {
        String fileName = uri.getLastPathSegment();
        if (fileName != null && fileName.contains(":")) {
            fileName = fileName.substring(fileName.lastIndexOf(":") + 1);
        }
        return fileName != null ? fileName : "document";
    }
    

    private void updatePresentationUI() {
        if (presentationPath != null) {
            String fileName = new File(presentationPath).getName();
            btnAddPresentation.setVisibility(Button.GONE);
            tvPresentationName.setText(fileName);
            tvPresentationName.setVisibility(TextView.VISIBLE);
            presentationControls.setVisibility(LinearLayout.VISIBLE);
        } else {
            btnAddPresentation.setVisibility(Button.VISIBLE);
            tvPresentationName.setVisibility(TextView.GONE);
            presentationControls.setVisibility(LinearLayout.GONE);
        }
    }
    
    private void updateRapportFinalUI() {
        if (rapportFinalPath != null) {
            String fileName = new File(rapportFinalPath).getName();
            btnAddRapportFinal.setVisibility(Button.GONE);
            tvRapportFinalName.setText(fileName);
            tvRapportFinalName.setVisibility(TextView.VISIBLE);
            rapportFinalControls.setVisibility(LinearLayout.VISIBLE);
        } else {
            btnAddRapportFinal.setVisibility(Button.VISIBLE);
            tvRapportFinalName.setVisibility(TextView.GONE);
            rapportFinalControls.setVisibility(LinearLayout.GONE);
        }
    }

    private void setupUploadButton() {
        btnUploadAll.setOnClickListener(v -> uploadDeliverables());
    }

    private void uploadDeliverables() {
        if (!hasFilesSelected()) {
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
            
            // Sauvegarder chaque fichier en BD
            new Thread(() -> {
                int successCount = 0;
                
                // Upload tous les rapports d'avancement (peut y en avoir plusieurs)
                for (String path : rapportAvancementPaths) {
                    successCount += uploadFile(pfaDossier.getPfa_id(), path, DeliverableFileType.RAPPORT_AVANCEMENT);
                }
                // Upload présentation et rapport final
                if (presentationPath != null) {
                    successCount += uploadFile(pfaDossier.getPfa_id(), presentationPath, DeliverableFileType.PRESENTATION);
                }
                if (rapportFinalPath != null) {
                    successCount += uploadFile(pfaDossier.getPfa_id(), rapportFinalPath, DeliverableFileType.RAPPORT_FINAL);
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
    
    private boolean hasFilesSelected() {
        if (isBeforeSoutenance) {
            // Avant soutenance: au moins un fichier parmi rapport(s) d'avancement, présentation ou rapport final
            return !rapportAvancementPaths.isEmpty() || presentationPath != null || rapportFinalPath != null;
        } else {
            // Après soutenance: au moins présentation ou rapport final
            return presentationPath != null || rapportFinalPath != null;
        }
    }
    
    private int uploadFile(long pfaId, String filePath, DeliverableFileType fileType) {
        try {
            Deliverable deliverable = new Deliverable();
            deliverable.setPfa_id(pfaId);
            deliverable.setFile_title(new File(filePath).getName());
            deliverable.setFile_uri(filePath);
            deliverable.setDeliverable_type(isBeforeSoutenance ? 
                DeliverableType.BEFORE_DEFENSE : DeliverableType.AFTER_DEFENSE);
            deliverable.setDeliverable_file_type(fileType);
            deliverable.setUploaded_at(System.currentTimeMillis());
            
            deliverableRepository.insert(deliverable, null);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
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
}

