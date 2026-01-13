package ma.ensate.pfa_manager.view.etudiant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import ma.ensate.pfa_manager.repository.SoutenanceRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SoutenanceFragment extends Fragment {
    
    private User currentUser;
    private PFADossierRepository pfaDossierRepository;
    private SoutenanceRepository soutenanceRepository;
    private TextView tvDate, tvLocation;
    private SwipeRefreshLayout swipeRefresh;
    
    private LiveData<Soutenance> soutenanceLiveData;
    private LiveData<PFADossier> pfaDossierLiveData;
    
    public static SoutenanceFragment newInstance(User user) {
        SoutenanceFragment fragment = new SoutenanceFragment();
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
        View view = inflater.inflate(R.layout.fragment_soutenance, container, false);
        
        pfaDossierRepository = new PFADossierRepository(requireActivity().getApplication());
        soutenanceRepository = new SoutenanceRepository(requireActivity().getApplication());
        
        tvDate = view.findViewById(R.id.tvSoutenanceDate);
        tvLocation = view.findViewById(R.id.tvSoutenanceLocation);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        
        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(() -> {
            loadSoutenanceData();
            swipeRefresh.setRefreshing(false);
        });
        
        setupLiveDataObservers();
        loadSoutenanceData();
        
        return view;
    }
    
    private void setupLiveDataObservers() {
        if (currentUser == null) {
            return;
        }
        
        AppDatabase db = AppDatabase.getInstance(requireActivity().getApplication());
        pfaDossierLiveData = db.pfaDossierDao().getPFAByStudentLive(currentUser.getUser_id());
        
        pfaDossierLiveData.observe(getViewLifecycleOwner(), pfaDossier -> {
            if (soutenanceLiveData != null) {
                soutenanceLiveData.removeObservers(getViewLifecycleOwner());
            }
            
            if (pfaDossier != null) {
                soutenanceLiveData = db.soutenanceDao().getSoutenanceByPFA(pfaDossier.getPfa_id());
                soutenanceLiveData.observe(getViewLifecycleOwner(), soutenance -> {
                    updateSoutenanceUI(soutenance);
                });
            } else {
                updateSoutenanceUI(null);
            }
        });
    }
    
    private void updateSoutenanceUI(Soutenance soutenance) {
        if (soutenance != null) {
            // Afficher la date
            if (soutenance.getDate_soutenance() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String dateStr = sdf.format(new Date(soutenance.getDate_soutenance()));
                tvDate.setText(dateStr);
            } else {
                tvDate.setText(R.string.not_scheduled);
            }
            
            // Afficher la salle
            if (soutenance.getLocation() != null && !soutenance.getLocation().isEmpty()) {
                tvLocation.setText(soutenance.getLocation());
            } else {
                tvLocation.setText(R.string.not_scheduled);
            }
        } else {
            // Aucune soutenance planifiée
            tvDate.setText(R.string.not_scheduled);
            tvLocation.setText(R.string.not_scheduled);
        }
    }
    
    private void loadSoutenanceData() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
            swipeRefresh.postDelayed(() -> {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
            }, 500);
        }
    }
}
