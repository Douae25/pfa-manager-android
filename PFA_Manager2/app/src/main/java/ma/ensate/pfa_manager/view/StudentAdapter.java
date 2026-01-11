package ma.ensate.pfa_manager.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.dto.StudentWithPFA;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private Context context;
    private List<StudentWithPFA> students = new ArrayList<>();
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(StudentWithPFA student);
    }

    public StudentAdapter(Context context) {
        this.context = context;
    }

    public void setOnStudentClickListener(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public void setStudents(List<StudentWithPFA> students) {
        this.students = students;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentWithPFA student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentInitials, tvStudentName, tvStudentEmail, tvPFATitle, tvPFAStatus;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            // ✅ CORRIGÉ : IDs correspondant au XML item_student.xml
            tvStudentInitials = itemView.findViewById(R.id.tvStudentInitials);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvPFATitle = itemView.findViewById(R.id.tvPFATitle);
            tvPFAStatus = itemView.findViewById(R.id.tvPFAStatus);
        }

        void bind(StudentWithPFA studentWithPFA) {
            // Initiales pour avatar
            String initials = getInitials(studentWithPFA);
            tvStudentInitials.setText(initials);

            // Nom complet
            tvStudentName.setText(studentWithPFA.getFullName());

            // Email
            tvStudentEmail.setText(studentWithPFA.getEmail());

            // Titre PFA
            if (studentWithPFA.hasPFA()) {
                tvPFATitle.setText(studentWithPFA.getPFATitle());
                tvPFATitle.setVisibility(View.VISIBLE);
            } else {
                tvPFATitle.setText("Aucun projet assigné");
                tvPFATitle.setVisibility(View.VISIBLE);
            }

            // Status
            if (studentWithPFA.hasPFA() && studentWithPFA.pfaDossier.getCurrent_status() != null) {
                setStatusBadge(tvPFAStatus, studentWithPFA.pfaDossier.getCurrent_status());
                tvPFAStatus.setVisibility(View.VISIBLE);
            } else {
                tvPFAStatus.setVisibility(View.GONE);
            }

            // Gestion du clic
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(studentWithPFA);
                } else {
                    // Fallback : Navigation directe
                    Intent intent = new Intent(context, StudentDetailActivity.class);
                    intent.putExtra("STUDENT_ID", getStudentId(studentWithPFA));
                    context.startActivity(intent);
                }
            });
        }

        // ✅ Méthode helper pour obtenir les initiales
        private String getInitials(StudentWithPFA studentWithPFA) {
            if (studentWithPFA.student == null) return "??";
            String firstName = studentWithPFA.student.getFirst_name();
            String lastName = studentWithPFA.student.getLast_name();
            String first = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "";
            String last = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1) : "";
            return (first + last).toUpperCase();
        }

        // ✅ Méthode helper pour obtenir l'ID étudiant
        private Long getStudentId(StudentWithPFA studentWithPFA) {
            return studentWithPFA.student != null ? studentWithPFA.student.getUser_id() : -1L;
        }

        private void setStatusBadge(TextView tvStatus, PFAStatus status) {
            switch (status) {
                case ASSIGNED:
                    tvStatus.setText("📋 Assigné");
                    tvStatus.setBackgroundResource(R.drawable.badge_status_assigned);
                    break;
                case CONVENTION_PENDING:
                    tvStatus.setText("⏳ Convention");
                    tvStatus.setBackgroundResource(R.drawable.badge_status_pending);
                    break;
                case IN_PROGRESS:
                    tvStatus.setText("🔄 En cours");
                    tvStatus.setBackgroundResource(R.drawable.badge_status_in_progress);
                    break;
                case CLOSED:
                    tvStatus.setText("✅ Clôturé");
                    tvStatus.setBackgroundResource(R.drawable.badge_status_closed);
                    break;
            }
        }
    }
}