package ma.ensate.pfa_manager.view.coordinateur_filiere.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.StudentWithEvaluation;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ElegantStudentViewHolder> {

    private List<StudentWithEvaluation> students = new ArrayList<>();

    @NonNull
    @Override
    public ElegantStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_etudiant, parent, false);
        return new ElegantStudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ElegantStudentViewHolder holder, int position) {
        StudentWithEvaluation student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void setStudents(List<StudentWithEvaluation> students) {
        this.students = students;
        notifyDataSetChanged();
    }

    static class ElegantStudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView textAvatar;
        private final TextView textStudentName;
        private final TextView textStudentEmail;
        private final TextView textScoreValue;
        private final TextView textStudentPhone;
        private final View viewStatus;
        private final TextView textStatus;
        private final TextView textSupervisor;

        public ElegantStudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textAvatar = itemView.findViewById(R.id.textAvatar);
            textStudentName = itemView.findViewById(R.id.textStudentName);
            textStudentEmail = itemView.findViewById(R.id.textStudentEmail);
            textScoreValue = itemView.findViewById(R.id.textScoreValue);
            textStudentPhone = itemView.findViewById(R.id.textStudentPhone);
            viewStatus = itemView.findViewById(R.id.viewStatus);
            textStatus = itemView.findViewById(R.id.textStatus);
            textSupervisor = itemView.findViewById(R.id.textSupervisor);
        }

        public void bind(StudentWithEvaluation student) {
            textAvatar.setText(student.getInitials());

            textStudentName.setText(student.getFullName());
            textStudentEmail.setText(student.getStudent().getEmail());

            textScoreValue.setText(student.getScoreText());

            String phone = student.getStudent().getPhone_number();
            textStudentPhone.setText(phone != null ? phone : "Non renseigné");

            String status = student.getStatus();
            textStatus.setText(status);
            viewStatus.setBackgroundColor(student.getStatusColor());

            textSupervisor.setText("Encadrant: " + student.getSupervisorName());

            itemView.setOnClickListener(v -> {
                // Ouvrir les details complets
            });
        }
    }
}