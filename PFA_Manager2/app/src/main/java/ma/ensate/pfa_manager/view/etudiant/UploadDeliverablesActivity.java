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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.DeliverableFile;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.view.etudiant.adapters.DeliverableFilesAdapter;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class UploadDeliverablesActivity extends AppCompatActivity {
    
    private static final int FILE_PICKER_REQUEST = 1001;
    
    private SettingsViewModel settingsViewModel;
    private User currentUser;
    private boolean isBeforeSoutenance;
    
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

        // TODO: Upload all files to server/storage
        
        Toast.makeText(this, R.string.deliverables_uploaded_success, Toast.LENGTH_LONG).show();
        finish();
    }
    
    private void updateEmptyState() {
        if (selectedFiles.isEmpty()) {
            recyclerViewFiles.setVisibility(RecyclerView.GONE);
        } else {
            recyclerViewFiles.setVisibility(RecyclerView.VISIBLE);
        }
    }
}
