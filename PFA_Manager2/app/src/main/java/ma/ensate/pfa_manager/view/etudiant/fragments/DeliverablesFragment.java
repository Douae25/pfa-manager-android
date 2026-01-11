package ma.ensate.pfa_manager.view.etudiant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.view.etudiant.UploadDeliverablesActivity;

public class DeliverablesFragment extends Fragment {
    
    private User currentUser;
    
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
        
        CardView cardUploadBefore = view.findViewById(R.id.cardUploadBefore);
        CardView cardUploadAfter = view.findViewById(R.id.cardUploadAfter);
        
        cardUploadBefore.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UploadDeliverablesActivity.class);
            intent.putExtra("user", currentUser);
            intent.putExtra("before_soutenance", true);
            startActivity(intent);
        });
        
        cardUploadAfter.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UploadDeliverablesActivity.class);
            intent.putExtra("user", currentUser);
            intent.putExtra("before_soutenance", false);
            startActivity(intent);
        });
        
        return view;
    }
}
