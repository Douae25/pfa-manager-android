package ma.ensate.pfa_manager.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.EvaluationCriteria;
import ma.ensate.pfa_manager.model.dto.CriteriaWithScore;

public class EvaluationFormDialog extends BottomSheetDialogFragment {

    private static final String ARG_PFA_ID = "pfa_id";
    private static final String ARG_PFA_TITLE = "pfa_title";
    private static final String ARG_STUDENT_NAME = "student_name";

    private Long pfaId;
    private String pfaTitle;
    private String studentName;

    private TextView tvPfaTitle, tvStudentName, tvTotalScore;
    private RecyclerView recyclerCriteria;
    private MaterialButton btnSave, btnCancel;

    private CriteriaAdapter criteriaAdapter;
    private List<EvaluationCriteria> criteriaList = new ArrayList<>();
    private OnSaveListener saveListener;

    public interface OnSaveListener {
        void onSave(Long pfaId, List<CriteriaWithScore> criteriaScores);
    }

    public static EvaluationFormDialog newInstance(Long pfaId, String pfaTitle, String studentName) {
        EvaluationFormDialog dialog = new EvaluationFormDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_PFA_ID, pfaId);
        args.putString(ARG_PFA_TITLE, pfaTitle);
        args.putString(ARG_STUDENT_NAME, studentName);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.saveListener = listener;
    }

    public void setCriteriaList(List<EvaluationCriteria> list) {
        this.criteriaList = list;
        if (criteriaAdapter != null) {
            List<CriteriaWithScore> withScores = new ArrayList<>();
            for (EvaluationCriteria c : list) {
                withScores.add(new CriteriaWithScore(c));
            }
            criteriaAdapter.setCriteriaList(withScores);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        if (getArguments() != null) {
            pfaId = getArguments().getLong(ARG_PFA_ID);
            pfaTitle = getArguments().getString(ARG_PFA_TITLE);
            studentName = getArguments().getString(ARG_STUDENT_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_evaluation_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPfaTitle = view.findViewById(R.id.tvPfaTitle);
        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvTotalScore = view.findViewById(R.id.tvTotalScore);
        recyclerCriteria = view.findViewById(R.id.recyclerCriteria);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        tvPfaTitle.setText(pfaTitle);
        tvStudentName.setText("Étudiant: " + studentName);

        criteriaAdapter = new CriteriaAdapter();
        criteriaAdapter.setOnScoreChangeListener(this::updateTotalScore);
        recyclerCriteria.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCriteria.setAdapter(criteriaAdapter);

        if (!criteriaList.isEmpty()) {
            List<CriteriaWithScore> withScores = new ArrayList<>();
            for (EvaluationCriteria c : criteriaList) {
                withScores.add(new CriteriaWithScore(c));
            }
            criteriaAdapter.setCriteriaList(withScores);
        }

        updateTotalScore();

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            if (saveListener != null) {
                saveListener.onSave(pfaId, criteriaAdapter.getCriteriaList());
            }
            dismiss();
        });
    }

    private void updateTotalScore() {
        double total = criteriaAdapter.calculateTotalScore();
        tvTotalScore.setText(String.format(Locale.FRENCH, "%.1f / 20", total));
    }
}