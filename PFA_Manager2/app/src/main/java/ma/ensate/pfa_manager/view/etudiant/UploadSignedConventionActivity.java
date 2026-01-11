package ma.ensate.pfa_manager.view.etudiant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class UploadSignedConventionActivity extends AppCompatActivity {
    
    private static final int FILE_PICKER_REQUEST = 1001;
    
    private SettingsViewModel settingsViewModel;
    private User currentUser;
    private ConventionRepository conventionRepository;
    private Uri selectedFileUri;
    
    private TextInputEditText inputFileName;
    private Button btnSelectFile, btnUpload, btnViewUploadedFile;
    private TextView tvSelectedFile;
    private String uploadedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory factory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
        
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_signed_convention);

        currentUser = (User) getIntent().getSerializableExtra("user");
        conventionRepository = new ConventionRepository(getApplication());

        setupBackNavigation();
        initViews();
        setupFileSelection();
        setupUploadButton();
        setupViewUploadedFileButton();
        loadExistingConvention();
    }

    private void setupBackNavigation() {
        ImageView backArrow = findViewById(R.id.backArrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        inputFileName = findViewById(R.id.inputFileName);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUpload = findViewById(R.id.btnUpload);
        btnViewUploadedFile = findViewById(R.id.btnViewUploadedFile);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
    }

    private void setupFileSelection() {
        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Sélectionner un fichier PDF"), FILE_PICKER_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                String fileName = selectedFileUri.getLastPathSegment();
                tvSelectedFile.setText(getString(R.string.file_selected, fileName));
                inputFileName.setText(fileName);
            }
        }
    }

    private void setupUploadButton() {
        btnUpload.setOnClickListener(v -> uploadConvention());
    }

    private void uploadConvention() {
        String fileName = inputFileName.getText().toString().trim();
        
        if (fileName.isEmpty() || selectedFileUri == null) {
            Toast.makeText(this, R.string.error_select_file, Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer l'ID de la convention depuis l'intent
        long conventionId = getIntent().getLongExtra("convention_id", -1);
        
        if (conventionId == -1) {
            Toast.makeText(this, R.string.error_no_convention, Toast.LENGTH_SHORT).show();
            return;
        }

        // Copier le fichier dans le stockage app avant de le sauvegarder
        new Thread(() -> {
            try {
                String savedFilePath = copyFileToAppStorage(selectedFileUri, fileName);
                if (savedFilePath != null) {
                    conventionRepository.getById(conventionId, convention -> {
                        if (convention != null) {
                            convention.setScanned_file_uri(savedFilePath);
                            convention.setState(ConventionState.UPLOADED);
                            conventionRepository.update(convention);
                            
                            runOnUiThread(() -> {
                                Toast.makeText(this, R.string.convention_uploaded_success, Toast.LENGTH_LONG).show();
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> 
                                Toast.makeText(this, R.string.error_no_convention, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                } else {
                    runOnUiThread(() -> 
                        Toast.makeText(this, R.string.error_select_file, Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                runOnUiThread(() -> 
                    Toast.makeText(this, R.string.error_select_file, Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void setupViewUploadedFileButton() {
        btnViewUploadedFile.setOnClickListener(v -> openUploadedFile());
    }

    private void openUploadedFile() {
        if (uploadedFileUri == null || uploadedFileUri.isEmpty()) {
            Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(uploadedFileUri);
            if (!file.exists()) {
                Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }
            
            Uri fileUri = FileProvider.getUriForFile(this, "ma.ensate.pfa_manager.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(Intent.createChooser(intent, getString(R.string.btn_view_uploaded_file)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private String copyFileToAppStorage(Uri sourceUri, String fileName) throws Exception {
        File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir == null) {
            return null;
        }
        
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String newFileName = "SignedConvention_" + timeStamp + ".pdf";
        File destFile = new File(downloadsDir, newFileName);
        
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

    private void loadExistingConvention() {
        long conventionId = getIntent().getLongExtra("convention_id", -1);
        
        if (conventionId != -1) {
            conventionRepository.getById(conventionId, convention -> {
                if (convention != null && convention.getScanned_file_uri() != null) {
                    runOnUiThread(() -> {
                        String uri = convention.getScanned_file_uri();
                        uploadedFileUri = uri;
                        String fileName = uri.substring(uri.lastIndexOf('/') + 1);
                        tvSelectedFile.setText(getString(R.string.file_already_uploaded, fileName));
                        tvSelectedFile.setBackgroundColor(0xFFE3F2FD);
                        inputFileName.setText(fileName);
                        btnViewUploadedFile.setVisibility(View.VISIBLE);
                    });
                }
            });
        }
    }
}
