package ma.ensate.pfa_manager.view.etudiant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ma.ensate.pfa_manager.R;
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
        
        loadSoutenanceData();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadSoutenanceData();
    }
    
    private void loadSoutenanceData() {
        if (currentUser == null) {
            return;
        }
        
        // Récupérer le dossier PFA de l'étudiant
        pfaDossierRepository.getByStudentId(currentUser.getUser_id(), pfaDossier -> {
            if (pfaDossier != null) {
                // Récupérer la soutenance liée à ce dossier
                soutenanceRepository.getByPfaId(pfaDossier.getPfa_id(), soutenance -> {
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
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
                        });
                    }
                });
            } else {
                // Aucun dossier PFA trouvé
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        tvDate.setText(R.string.not_scheduled);
                        tvLocation.setText(R.string.not_scheduled);
                    });
                }
            }
        });
    }
}
