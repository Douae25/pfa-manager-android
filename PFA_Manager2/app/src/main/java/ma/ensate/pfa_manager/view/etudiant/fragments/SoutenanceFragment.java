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

public class SoutenanceFragment extends Fragment {
    
    private User currentUser;
    
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
        
        TextView tvDate = view.findViewById(R.id.tvSoutenanceDate);
        TextView tvLocation = view.findViewById(R.id.tvSoutenanceLocation);
        
        // TODO: Charger les infos depuis la BD
        
        return view;
    }
}
