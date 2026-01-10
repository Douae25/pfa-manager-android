package ma.ensate.pfa_manager.view;

import android.content.Intent;
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
import com.google.android.material.card.MaterialCardView;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.view.adapter.ConventionAdapter;
import ma.ensate.pfa_manager.view.dialog.RejectionDialogHelper;
import ma.ensate.pfa_manager.viewmodel.AdminViewModel;
import ma.ensate.pfa_manager.viewmodel.AdminViewModelFactory;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;

public class AdminActivity extends AppCompatActivity {

    private AdminViewModel adminViewModel;
    private SettingsViewModel settingsViewModel;
    private RecyclerView conventionRecyclerView;
    private ConventionAdapter conventionAdapter;
    private MaterialCardView cardConventionRequests, cardSignedConventions, cardManageUsers, cardLogout;
    private LinearLayout emptyStateView;
    private int currentView = 0; // 0: requests, 1: signed, 2: users

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup language first
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory settingsFactory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, settingsFactory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        setupLanguageToggle();
        setupAdminViewModel();
        setupUI();
        setupNavigation();
    }

    private void setupLanguageToggle() {
        TextView langFr = findViewById(R.id.langFr);
        TextView langEn = findViewById(R.id.langEn);
        langFr.setOnClickListener(v -> {
            settingsViewModel.changeLanguage("fr");
            recreate();
        });
        langEn.setOnClickListener(v -> {
            settingsViewModel.changeLanguage("en");
            recreate();
        });
    }

    private void setupAdminViewModel() {
        ConventionRepository conventionRepository = new ConventionRepository(getApplication());
        UserRepository userRepository = new UserRepository(getApplication());
        AdminViewModelFactory factory = new AdminViewModelFactory(conventionRepository, userRepository);
        adminViewModel = new ViewModelProvider(this, factory).get(AdminViewModel.class);

        // Observe pending conventions
        adminViewModel.getPendingConventions().observe(this, conventions -> {
            if (currentView == 0) {
                if (conventions != null && !conventions.isEmpty()) {
                    conventionAdapter = new ConventionAdapter(conventions, new ConventionAdapter.ConventionActionListener() {
                        @Override
                        public void onApprove(ma.ensate.pfa_manager.model.Convention convention) {
                            adminViewModel.approveConvention(convention);
                            adminViewModel.loadPendingConventions();
                        }

                        @Override
                        public void onReject(ma.ensate.pfa_manager.model.Convention convention) {
                            RejectionDialogHelper.showRejectionDialog(AdminActivity.this, new RejectionDialogHelper.RejectionListener() {
                                @Override
                                public void onRejectionConfirmed(String reason) {
                                    adminViewModel.rejectConvention(convention, reason);
                                    adminViewModel.loadPendingConventions();
                                }

                                @Override
                                public void onRejectionCancelled() {
                                }
                            });
                        }

                        @Override
                        public void onValidate(ma.ensate.pfa_manager.model.Convention convention) {
                        }

                        @Override
                        public void onRejectUploaded(ma.ensate.pfa_manager.model.Convention convention) {
                        }
                    }, true);
                    conventionRecyclerView.setAdapter(conventionAdapter);
                    updateConventionList(false);
                } else {
                    updateConventionList(true);
                }
            }
        });

        // Observe signed conventions
        adminViewModel.getSignedConventions().observe(this, conventions -> {
            if (currentView == 1) {
                if (conventions != null && !conventions.isEmpty()) {
                    conventionAdapter = new ConventionAdapter(conventions, new ConventionAdapter.ConventionActionListener() {
                        @Override
                        public void onApprove(ma.ensate.pfa_manager.model.Convention convention) {
                        }

                        @Override
                        public void onReject(ma.ensate.pfa_manager.model.Convention convention) {
                        }

                        @Override
                        public void onValidate(ma.ensate.pfa_manager.model.Convention convention) {
                            adminViewModel.validateUploadedConvention(convention);
                            adminViewModel.loadSignedConventions();
                        }

                        @Override
                        public void onRejectUploaded(ma.ensate.pfa_manager.model.Convention convention) {
                            RejectionDialogHelper.showRejectionDialog(AdminActivity.this, new RejectionDialogHelper.RejectionListener() {
                                @Override
                                public void onRejectionConfirmed(String reason) {
                                    adminViewModel.rejectUploadedConvention(convention, reason);
                                    adminViewModel.loadSignedConventions();
                                }

                                @Override
                                public void onRejectionCancelled() {
                                }
                            });
                        }
                    }, false);
                    conventionRecyclerView.setAdapter(conventionAdapter);
                    updateConventionList(false);
                } else {
                    updateConventionList(true);
                }
            }
        });

        // Observe users list
        adminViewModel.getAllUsers().observe(this, users -> {
            if (currentView == 2) {
                updateUsersList(users.isEmpty());
            }
        });

        // Observe action results
        adminViewModel.getActionResult().observe(this, result -> {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupUI() {
        conventionRecyclerView = findViewById(R.id.conventionRecyclerView);
        emptyStateView = findViewById(R.id.emptyStateView);
        cardConventionRequests = findViewById(R.id.cardConventionRequests);
        cardSignedConventions = findViewById(R.id.cardSignedConventions);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardLogout = findViewById(R.id.cardLogout);

        if (conventionRecyclerView != null) {
            conventionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            Log.w("AdminActivity", "conventionRecyclerView is null in setupUI");
        }

        // Load initial data
        loadConventionRequests();
    }

    private void setupNavigation() {
        if (cardConventionRequests != null) cardConventionRequests.setOnClickListener(v -> {
            try {
                android.content.Intent intent = new android.content.Intent(AdminActivity.this, ConventionListActivity.class);
                intent.putExtra(ConventionListActivity.EXTRA_MODE, "PENDING");
                startActivity(intent);
            } catch (Exception e) {
                Log.e("AdminActivity", "Failed to start ConventionListActivity", e);
            }
        });
        else Log.w("AdminActivity", "cardConventionRequests is null in setupNavigation");

        if (cardSignedConventions != null) cardSignedConventions.setOnClickListener(v -> {
            try {
                android.content.Intent intent = new android.content.Intent(AdminActivity.this, ConventionListActivity.class);
                intent.putExtra(ConventionListActivity.EXTRA_MODE, "UPLOADED");
                startActivity(intent);
            } catch (Exception e) {
                Log.e("AdminActivity", "Failed to start ConventionListActivity", e);
            }
        });
        else Log.w("AdminActivity", "cardSignedConventions is null in setupNavigation");

        if (cardManageUsers != null) cardManageUsers.setOnClickListener(v -> loadUsers());
        else Log.w("AdminActivity", "cardManageUsers is null in setupNavigation");

        if (cardLogout != null) cardLogout.setOnClickListener(v -> logout());
        else Log.w("AdminActivity", "cardLogout is null in setupNavigation");
    }

    private void loadConventionRequests() {
        currentView = 0;
        adminViewModel.loadPendingConventions();
        highlightCard(cardConventionRequests);
    }

    private void loadSignedConventions() {
        currentView = 1;
        adminViewModel.loadSignedConventions();
        highlightCard(cardSignedConventions);
    }

    private void loadUsers() {
        currentView = 2;
        adminViewModel.loadAllUsers();
        highlightCard(cardManageUsers);
    }

    private void logout() {
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void highlightCard(MaterialCardView activeCard) {
        try {
            int transparent = getResources().getColor(android.R.color.transparent);
            if (cardConventionRequests != null) cardConventionRequests.setStrokeColor(transparent);
            if (cardSignedConventions != null) cardSignedConventions.setStrokeColor(transparent);
            if (cardManageUsers != null) cardManageUsers.setStrokeColor(transparent);
            if (cardLogout != null) cardLogout.setStrokeColor(transparent);

            if (activeCard != null) {
                activeCard.setStrokeColor(getResources().getColor(android.R.color.holo_blue_light));
                activeCard.setStrokeWidth(3);
            } else {
                Log.w("AdminActivity", "activeCard is null in highlightCard");
            }
        } catch (Exception e) {
            Log.e("AdminActivity", "Error in highlightCard", e);
        }
    }

    private void updateConventionList(boolean isEmpty) {
        if (isEmpty) {
            conventionRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            conventionRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void updateUsersList(boolean isEmpty) {
        if (isEmpty) {
            conventionRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            conventionRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }
}
