package ma.ensate.pfa_manager.view.etudiant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

public class UploadSignedConventionActivity extends AppCompatActivity {
    
    private static final int FILE_PICKER_REQUEST = 1001;
    
    private SettingsViewModel settingsViewModel;
    private User currentUser;
    private ConventionRepository conventionRepository;
    private Uri selectedFileUri;
    
    private TextInputEditText inputFileName;
    private Button btnSelectFile, btnUpload;
    private TextView tvSelectedFile;

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
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
    }

    private void setupFileSelection() {
        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, FILE_PICKER_REQUEST);
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

        // TODO: Upload file to server/storage
        // For now, we'll just update the convention state
        
        // Vérifier si une convention existe pour cet utilisateur
        conventionRepository.getByPfaId(currentUser.getUser_id(), convention -> {
            if (convention != null) {
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
    }
}
