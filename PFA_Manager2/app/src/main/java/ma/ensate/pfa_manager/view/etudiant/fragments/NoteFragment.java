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

public class NoteFragment extends Fragment {
    
    private User currentUser;
    
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
        
        TextView tvFinalNote = view.findViewById(R.id.tvFinalNote);
        
        // TODO: Charger la note depuis la BD
        
        return view;
    }
}
