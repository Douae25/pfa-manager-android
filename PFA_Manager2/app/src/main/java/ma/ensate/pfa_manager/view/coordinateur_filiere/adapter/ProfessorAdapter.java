package ma.ensate.pfa_manager.view.coordinateur_filiere.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.ProfessorWithStats;
import java.util.ArrayList;
import java.util.List;

public class ProfessorAdapter extends RecyclerView.Adapter<ProfessorAdapter.ElegantProfessorViewHolder> {

    private List<ProfessorWithStats> professors = new ArrayList<>();

    @NonNull
    @Override
    public ElegantProfessorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_professor, parent, false);
        return new ElegantProfessorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ElegantProfessorViewHolder holder, int position) {
        ProfessorWithStats professor = professors.get(position);
        holder.bind(professor);
    }

    @Override
    public int getItemCount() {
        return professors.size();
    }

    public void setProfessors(List<ProfessorWithStats> professors) {
        this.professors = professors;
        notifyDataSetChanged();
    }

    static class ElegantProfessorViewHolder extends RecyclerView.ViewHolder {
        private final TextView textAvatar;
        private final TextView textProfessorName;
        private final TextView textProfessorEmail;
        private final TextView textProfessorSpecialty;
        private final TextView textStudentsCount;
        private final TextView textAverageScore;
        private final TextView textProfessorPhone;

        public ElegantProfessorViewHolder(@NonNull View itemView) {
            super(itemView);
            textAvatar = itemView.findViewById(R.id.textAvatar);
            textProfessorName = itemView.findViewById(R.id.textProfessorName);
            textProfessorEmail = itemView.findViewById(R.id.textProfessorEmail);
            textProfessorSpecialty = itemView.findViewById(R.id.textProfessorSpecialty);
            textStudentsCount = itemView.findViewById(R.id.textStudentsCount);
            textAverageScore = itemView.findViewById(R.id.textAverageScore);
            textProfessorPhone = itemView.findViewById(R.id.textProfessorPhone);
        }

        public void bind(ProfessorWithStats professor) {
            textAvatar.setText(professor.getInitials());

            textProfessorName.setText(professor.getFullName());
            textProfessorEmail.setText(professor.getProfessor().getEmail());

            textProfessorSpecialty.setText("Encadrant PFA");

            textStudentsCount.setText(String.valueOf(professor.getStudentCount()));
            textAverageScore.setText(professor.getAverageScoreText());

            String phone = professor.getProfessor().getPhone_number();
            textProfessorPhone.setText(phone != null ? phone : "Non renseigné");

            itemView.setOnClickListener(v -> {
                //Ouvrir les details complets
            });
        }
    }
}