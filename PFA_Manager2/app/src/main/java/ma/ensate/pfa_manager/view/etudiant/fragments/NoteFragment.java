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
import ma.ensate.pfa_manager.repository.EvaluationRepository;
import ma.ensate.pfa_manager.repository.PFADossierRepository;
import java.util.List;
import ma.ensate.pfa_manager.model.Evaluation;

public class NoteFragment extends Fragment {
    
    private User currentUser;
    private PFADossierRepository pfaDossierRepository;
    private EvaluationRepository evaluationRepository;
    private TextView tvFinalNote;
    
    public static NoteFragment newInstance(User user) {
        NoteFragment fragment = new NoteFragment();
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
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        
        pfaDossierRepository = new PFADossierRepository(requireActivity().getApplication());
        evaluationRepository = new EvaluationRepository(requireActivity().getApplication());
        
        tvFinalNote = view.findViewById(R.id.tvFinalNote);
        
        loadFinalNote();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadFinalNote();
    }
    
    private void loadFinalNote() {
        if (currentUser == null) {
            return;
        }
        
        // Récupérer le dossier PFA de l'étudiant
        pfaDossierRepository.getByStudentId(currentUser.getUser_id(), pfaDossier -> {
            if (pfaDossier != null) {
                // Récupérer les évaluations liées à ce dossier
                evaluationRepository.getByPfaId(pfaDossier.getPfa_id(), evaluations -> {
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (evaluations != null && !evaluations.isEmpty()) {
                                // Calculer la moyenne des notes ou prendre la dernière
                                double totalScore = 0;
                                int count = 0;
                                
                                for (Evaluation eval : evaluations) {
                                    if (eval.getTotal_score() != null) {
                                        totalScore += eval.getTotal_score();
                                        count++;
                                    }
                                }
                                
                                if (count > 0) {
                                    double averageScore = totalScore / count;
                                    tvFinalNote.setText(String.format("%.2f / 20", averageScore));
                                } else {
                                    tvFinalNote.setText(R.string.not_evaluated);
                                }
                            } else {
                                // Aucune évaluation trouvée
                                tvFinalNote.setText(R.string.not_evaluated);
                            }
                        });
                    }
                });
            } else {
                // Aucun dossier PFA trouvé
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        tvFinalNote.setText(R.string.not_evaluated);
                    });
                }
            }
        });
    }
}
