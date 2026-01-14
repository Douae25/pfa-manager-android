package ma.ensate.pfa_manager.view.etudiant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.view.etudiant.UploadDeliverablesActivity;
import ma.ensate.pfa_manager.view.etudiant.ViewDeliverablesActivity;

public class DeliverablesFragment extends Fragment {
    
    private User currentUser;
    private PFADossierRepository pfaDossierRepository;
    private PFADossier currentPfa;
    private CardView cardUploadBefore;
    private CardView cardUploadAfter;
    private CardView cardViewDeliverables;
    private SwipeRefreshLayout swipeRefresh;
    private LiveData<PFADossier> pfaLiveData;
    
    public static DeliverablesFragment newInstance(User user) {
        DeliverablesFragment fragment = new DeliverablesFragment();
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
        View view = inflater.inflate(R.layout.fragment_deliverables, container, false);
        
        cardUploadBefore = view.findViewById(R.id.cardUploadBefore);
        cardUploadAfter = view.findViewById(R.id.cardUploadAfter);
        cardViewDeliverables = view.findViewById(R.id.cardViewDeliverables);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        pfaDossierRepository = new PFADossierRepository(requireActivity().getApplication());
        
        cardUploadBefore.setOnClickListener(v -> {
            if (isUploadAllowed()) {
                Intent intent = new Intent(getActivity(), UploadDeliverablesActivity.class);
                intent.putExtra("user", currentUser);
                intent.putExtra("before_soutenance", true);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Disponible seulement lorsque le PFA est en cours.", Toast.LENGTH_SHORT).show();
            }
        });
        
        cardUploadAfter.setOnClickListener(v -> {
            if (isUploadAllowed()) {
                Intent intent = new Intent(getActivity(), UploadDeliverablesActivity.class);
                intent.putExtra("user", currentUser);
                intent.putExtra("before_soutenance", false);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Disponible seulement lorsque le PFA est en cours.", Toast.LENGTH_SHORT).show();
            }
        });
        
        cardViewDeliverables.setOnClickListener(v -> {
            if (currentPfa != null) {
                Intent intent = new Intent(getActivity(), ViewDeliverablesActivity.class);
                intent.putExtra("user", currentUser);
                intent.putExtra("pfa_id", currentPfa.getPfa_id());
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Aucun dossier PFA trouvé.", Toast.LENGTH_SHORT).show();
            }
        });
        
        swipeRefresh.setOnRefreshListener(() -> {
            // LiveData mettra à jour automatiquement; on stoppe juste l'animation
            swipeRefresh.postDelayed(() -> swipeRefresh.setRefreshing(false), 300);
        });
        
        setupLiveDataObservers();
        return view;
    }

    private void setupLiveDataObservers() {
        if (currentUser == null) {
            return;
        }
        AppDatabase db = AppDatabase.getInstance(requireActivity().getApplication());
        pfaLiveData = db.pfaDossierDao().getPFAByStudentLive(currentUser.getUser_id());
        pfaLiveData.observe(getViewLifecycleOwner(), pfa -> {
            currentPfa = pfa;
            updateUploadAvailability();
        });
    }

    private boolean isUploadAllowed() {
        return currentPfa != null && currentPfa.getCurrent_status() == PFAStatus.IN_PROGRESS;
    }

    private void updateUploadAvailability() {
        boolean allowed = isUploadAllowed();
        cardUploadBefore.setEnabled(allowed);
        cardUploadAfter.setEnabled(allowed);
        float alpha = allowed ? 1f : 0.5f;
        cardUploadBefore.setAlpha(alpha);
        cardUploadAfter.setAlpha(alpha);
    }
}
