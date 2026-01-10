package ma.ensate.pfa_manager.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
import ma.ensate.pfa_manager.model.Convention;

public class ConventionListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode"; // "PENDING" or "UPLOADED"

    private AdminViewModel adminViewModel;
    private RecyclerView conventionRecyclerView;
    private LinearLayout emptyStateView;
    private ConventionAdapter conventionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convention_list);

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
