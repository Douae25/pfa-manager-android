package ma.ensate.pfa_manager.view.etudiant.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.utils.ConventionPdfGenerator;
import ma.ensate.pfa_manager.view.etudiant.ConventionRequestActivity;
import ma.ensate.pfa_manager.view.etudiant.UploadSignedConventionActivity;

public class ConventionFragment extends Fragment {
    
    private static final int WRITE_PERMISSION_REQUEST = 1001;
    
    private User currentUser;
    private PFADossierRepository pfaDossierRepository;
    private ConventionRepository conventionRepository;
    private PFADossier currentPfaDossier;
    private Convention currentConvention;
    
    private CardView cardRequestConvention;
    private CardView cardViewConvention;
    private CardView cardUploadSigned;
    private TextView tvConventionStatus;
    private TextView tvConventionReason;
    private SwipeRefreshLayout swipeRefresh;
    
    private LiveData<Convention> conventionLiveData;
    private LiveData<PFADossier> pfaDossierLiveData;
    
    public static ConventionFragment newInstance(User user) {
        ConventionFragment fragment = new ConventionFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("user");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_convention, container, false);
        
        pfaDossierRepository = new PFADossierRepository(requireActivity().getApplication());
        conventionRepository = new ConventionRepository(requireActivity().getApplication());
        
        cardRequestConvention = view.findViewById(R.id.cardRequestConvention);
        cardViewConvention = view.findViewById(R.id.cardViewConvention);
        cardUploadSigned = view.findViewById(R.id.cardUploadSigned);
        tvConventionStatus = view.findViewById(R.id.tvConventionStatus);
        tvConventionReason = view.findViewById(R.id.tvConventionReason);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        
        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(() -> {
            loadConventionData();
            swipeRefresh.setRefreshing(false);
        });
        
        // Charger les données avec LiveData
        setupLiveDataObservers();
        loadConventionData();
        
        setupCardClickListeners();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadConventionData();
    }
    
    private void setupLiveDataObservers() {
        if (currentUser == null) {
            return;
        }
        
        // Observer pour le PFADossier
        AppDatabase db = AppDatabase.getInstance(requireActivity().getApplication());
        pfaDossierLiveData = db.pfaDossierDao().getPFAByStudentLive(currentUser.getUser_id());
        
        pfaDossierLiveData.observe(getViewLifecycleOwner(), pfaDossier -> {
            currentPfaDossier = pfaDossier;
            
            // Supprimer l'ancien observer de convention si existe
            if (conventionLiveData != null) {
                conventionLiveData.removeObservers(getViewLifecycleOwner());
            }
            
            if (pfaDossier != null) {
                // Observer pour la convention
                conventionLiveData = db.conventionDao().getConventionByPFA(pfaDossier.getPfa_id());
                conventionLiveData.observe(getViewLifecycleOwner(), convention -> {
                    currentConvention = convention;
                    updateConventionStatus();
                });
            } else {
                currentConvention = null;
                updateConventionStatus();
            }
        });
    }
    private void loadConventionData() {
        // Cette méthode force un refresh si nécessaire
        // Les observers LiveData se chargeront automatiquement du reste
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
            swipeRefresh.postDelayed(() -> {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
            }, 500);
        }
    }

    private void updateConventionStatus() {
        if (currentPfaDossier == null) {
            // Aucune demande de convention
            tvConventionStatus.setVisibility(View.VISIBLE);
            tvConventionStatus.setText(R.string.convention_not_requested);
            tvConventionStatus.setTextColor(ContextCompat.getColor(requireActivity(),
                android.R.color.holo_orange_dark));
            cardViewConvention.setEnabled(false);
            cardUploadSigned.setEnabled(false);
            tvConventionReason.setVisibility(View.GONE);
            return;
        }

        // Pas de convention enregistrée
        if (currentConvention == null) {
            tvConventionStatus.setVisibility(View.GONE);
            cardViewConvention.setEnabled(false);
            cardUploadSigned.setEnabled(false);
            tvConventionReason.setVisibility(View.GONE);
            return;
        }

        ConventionState state = currentConvention.getState();

        if (state == ConventionState.PENDING) {
            // Demande en cours
            tvConventionStatus.setVisibility(View.VISIBLE);
            tvConventionStatus.setText(R.string.convention_pending);
            tvConventionStatus.setTextColor(ContextCompat.getColor(requireActivity(),
                android.R.color.holo_orange_dark));
            cardViewConvention.setEnabled(false);
            cardUploadSigned.setEnabled(false);
            tvConventionReason.setVisibility(View.GONE);
            return;
        }

        if (state == ConventionState.VALIDATED || state == ConventionState.GENERATED || state == ConventionState.UPLOADED) {
            // Convention disponible / validée
            tvConventionStatus.setVisibility(View.VISIBLE);
            tvConventionStatus.setText(R.string.convention_validated);
            tvConventionStatus.setTextColor(ContextCompat.getColor(requireActivity(),
                android.R.color.holo_green_dark));
            cardViewConvention.setEnabled(true);
            cardUploadSigned.setEnabled(true);
            tvConventionReason.setVisibility(View.GONE);
            return;
        }

        if (state == ConventionState.REFUSED || state == ConventionState.REJECTED) {
            // Convention refusée (demande) ou rejetée (signée)
            tvConventionStatus.setVisibility(View.VISIBLE);
            if (state == ConventionState.REFUSED) {
                tvConventionStatus.setText(R.string.convention_rejected_request);
            } else {
                tvConventionStatus.setText(R.string.convention_rejected);
            }
            tvConventionStatus.setTextColor(ContextCompat.getColor(requireActivity(),
                android.R.color.holo_red_dark));
            cardViewConvention.setEnabled(true);
            // Upload only allowed when signed version was rejected, not when request was refused
            cardUploadSigned.setEnabled(state == ConventionState.REJECTED);

            String reason = currentConvention.getAdmin_comment();
            if (TextUtils.isEmpty(reason)) {
                reason = getString(R.string.convention_reason_default);
            }
            tvConventionReason.setText(getString(R.string.convention_reason_prefix, reason));
            tvConventionReason.setVisibility(View.VISIBLE);
            return;
        }

        // État indéterminé
        tvConventionStatus.setVisibility(View.GONE);
        cardViewConvention.setEnabled(false);
        cardUploadSigned.setEnabled(false);
        tvConventionReason.setVisibility(View.GONE);
    }
    
    private void setupCardClickListeners() {
        // Demander une convention
        cardRequestConvention.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ConventionRequestActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });
        
        // Voir/télécharger la convention
        cardViewConvention.setOnClickListener(v -> {
            // Si aucune demande n'existe
            if (currentPfaDossier == null) {
                Toast.makeText(requireActivity(), R.string.convention_not_requested, Toast.LENGTH_SHORT).show();
                return;
            }

            // Si la convention est générée / uploadée / validée / rejetée, autoriser le téléchargement
            if (currentConvention != null && (currentConvention.getState() == ConventionState.VALIDATED
                    || currentConvention.getState() == ConventionState.GENERATED
                    || currentConvention.getState() == ConventionState.UPLOADED
                    || currentConvention.getState() == ConventionState.REJECTED)) {
                // Générer et sauvegarder (pas de permission requise : stockage app)
                generateAndDownloadPdf();
            } else {
                // Dans tous les autres cas, afficher que la demande n'a pas encore été traitée
                Toast.makeText(requireActivity(), R.string.convention_pending_message, Toast.LENGTH_LONG).show();
            }
        });
        
        // Télécharger la convention signée
        cardUploadSigned.setOnClickListener(v -> {
            // Empêcher l'accès si la convention n'existe pas, est en cours, ou si la demande a été refusée
            if (currentConvention == null || currentConvention.getState() == ConventionState.PENDING
                    || currentConvention.getState() == ConventionState.REFUSED) {
                Toast.makeText(requireActivity(), R.string.cannot_upload_unsigned_convention, Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(requireActivity(), UploadSignedConventionActivity.class);
            intent.putExtra("convention_id", currentConvention.getConvention_id());
            startActivity(intent);
        });
    }
    
    private void generateAndDownloadPdf() {
        if (currentConvention == null) {
            Toast.makeText(requireActivity(), R.string.convention_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(requireActivity(), R.string.convention_downloading, Toast.LENGTH_SHORT).show();
        
        ConventionPdfGenerator.generateAndDownloadPdf(requireActivity(), currentConvention);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndDownloadPdf();
            } else {
                Toast.makeText(requireActivity(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
