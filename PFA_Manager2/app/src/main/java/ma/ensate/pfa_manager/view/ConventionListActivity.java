package ma.ensate.pfa_manager.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.view.adapter.ConventionAdapter;
import ma.ensate.pfa_manager.view.dialog.RejectionDialogHelper;
import ma.ensate.pfa_manager.viewmodel.AdminViewModel;
import ma.ensate.pfa_manager.viewmodel.AdminViewModelFactory;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;
import ma.ensate.pfa_manager.model.Convention;

public class ConventionListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode"; // "PENDING" or "UPLOADED"

    private AdminViewModel adminViewModel;
    private SettingsViewModel settingsViewModel;
    private RecyclerView conventionRecyclerView;
    private LinearLayout emptyStateView;
    private ConventionAdapter conventionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup language first
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory settingsFactory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, settingsFactory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convention_list);

        setupLanguageToggle();

        conventionRecyclerView = findViewById(R.id.conventionRecyclerView);
        emptyStateView = findViewById(R.id.emptyStateView);
        conventionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ConventionRepository conventionRepository = new ConventionRepository(getApplication());
        UserRepository userRepository = new UserRepository(getApplication());
        AdminViewModelFactory factory = new AdminViewModelFactory(conventionRepository, userRepository);
        adminViewModel = new ViewModelProvider(this, factory).get(AdminViewModel.class);

        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = "PENDING";

        if (mode.equalsIgnoreCase("PENDING")) {
            adminViewModel.getPendingConventions().observe(this, conventions -> {
                updateListForMode(conventions, true);
            });
            adminViewModel.loadPendingConventions();
            setTitle(getString(R.string.admin_convention_requests));
        } else {
            adminViewModel.getSignedConventions().observe(this, conventions -> {
                updateListForMode(conventions, false);
            });
            adminViewModel.loadSignedConventions();
            setTitle(getString(R.string.admin_signed_conventions));
        }

        adminViewModel.getActionResult().observe(this, result -> {
            if (result != null) Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupLanguageToggle() {
        TextView langFr = findViewById(R.id.langFr);
        TextView langEn = findViewById(R.id.langEn);
        Log.d("ConventionListActivity", "langFr: " + (langFr == null ? "NULL" : "found"));
        Log.d("ConventionListActivity", "langEn: " + (langEn == null ? "NULL" : "found"));
        if (langFr != null) {
            langFr.setOnClickListener(v -> {
                Log.d("ConventionListActivity", "FR clicked");
                settingsViewModel.changeLanguage("fr");
                recreate();
            });
        }
        if (langEn != null) {
            langEn.setOnClickListener(v -> {
                Log.d("ConventionListActivity", "EN clicked");
                settingsViewModel.changeLanguage("en");
                recreate();
            });
        }
    }

    private void updateListForMode(java.util.List<Convention> conventions, boolean pendingMode) {
        if (conventions != null && !conventions.isEmpty()) {
            conventionAdapter = new ConventionAdapter(conventions, new ConventionAdapter.ConventionActionListener() {
                @Override
                public void onApprove(Convention convention) {
                    adminViewModel.approveConvention(convention);
                    if (pendingMode) adminViewModel.loadPendingConventions();
                    else adminViewModel.loadSignedConventions();
                }

                @Override
                public void onReject(Convention convention) {
                    RejectionDialogHelper.showRejectionDialog(ConventionListActivity.this, new RejectionDialogHelper.RejectionListener() {
                        @Override
                        public void onRejectionConfirmed(String reason) {
                            adminViewModel.rejectConvention(convention, reason);
                            if (pendingMode) adminViewModel.loadPendingConventions();
                            else adminViewModel.loadSignedConventions();
                        }

                        @Override
                        public void onRejectionCancelled() {}
                    });
                }

                @Override
                public void onValidate(Convention convention) {
                    adminViewModel.validateUploadedConvention(convention);
                    adminViewModel.loadSignedConventions();
                }

                @Override
                public void onRejectUploaded(Convention convention) {
                    RejectionDialogHelper.showRejectionDialog(ConventionListActivity.this, new RejectionDialogHelper.RejectionListener() {
                        @Override
                        public void onRejectionConfirmed(String reason) {
                            adminViewModel.rejectUploadedConvention(convention, reason);
                            adminViewModel.loadSignedConventions();
                        }

                        @Override
                        public void onRejectionCancelled() {}
                    });
                }

                @Override
                public void onViewDetails(Convention convention) {
                    try {
                        android.content.Intent intent = new android.content.Intent(ConventionListActivity.this, ConventionDetailActivity.class);
                        intent.putExtra("convention_id", convention.getConvention_id());
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("ConventionListActivity", "Failed to start ConventionDetailActivity", e);
                    }
                }
            }, pendingMode);

            conventionRecyclerView.setAdapter(conventionAdapter);
            conventionRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            conventionRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        }
    }
}
