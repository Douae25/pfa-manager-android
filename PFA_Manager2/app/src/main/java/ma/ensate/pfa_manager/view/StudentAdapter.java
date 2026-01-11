package ma.ensate.pfa_manager.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

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
        TextView tvStudentInitials, tvStudentName, tvStudentEmail, tvPFATitle;
        Chip chipPFAStatus;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentInitials = itemView.findViewById(R.id.tvStudentInitials);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvPFATitle = itemView.findViewById(R.id.tvPFATitle);
            chipPFAStatus = itemView.findViewById(R.id.tvPFAStatus);
        }

        void bind(StudentWithPFA studentWithPFA) {
            String initials = getInitials(studentWithPFA);
            tvStudentInitials.setText(initials);

            tvStudentName.setText(studentWithPFA.getFullName());

            tvStudentEmail.setText(studentWithPFA.getEmail());

            if (studentWithPFA.hasPFA()) {
                tvPFATitle.setText(studentWithPFA.getPFATitle());
                tvPFATitle.setVisibility(View.VISIBLE);
            } else {
                tvPFATitle.setText("Aucun projet assigné");
                tvPFATitle.setVisibility(View.VISIBLE);
            }

            if (studentWithPFA.hasPFA() && studentWithPFA.pfaDossier.getCurrent_status() != null) {
                setStatusChip(chipPFAStatus, studentWithPFA.pfaDossier.getCurrent_status());
                chipPFAStatus.setVisibility(View.VISIBLE);
            } else {
                chipPFAStatus.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(studentWithPFA);
                } else {
                    Intent intent = new Intent(context, StudentDetailActivity.class);
                    intent.putExtra("STUDENT_ID", getStudentId(studentWithPFA));
                    context.startActivity(intent);
                }
            });
        }

        private String getInitials(StudentWithPFA studentWithPFA) {
            if (studentWithPFA.student == null) return "??";
            String firstName = studentWithPFA.student.getFirst_name();
            String lastName = studentWithPFA.student.getLast_name();
            String first = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "";
            String last = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1) : "";
            return (first + last).toUpperCase();
        }

        private Long getStudentId(StudentWithPFA studentWithPFA) {
            return studentWithPFA.student != null ? studentWithPFA.student.getUser_id() : -1L;
        }

        private void setStatusChip(Chip chip, PFAStatus status) {
            switch (status) {
                case ASSIGNED:
                    chip.setText("Assigné");
                    chip.setChipBackgroundColorResource(R.color.primary_light);
                    chip.setTextColor(context.getColor(R.color.primary));
                    break;
                case CONVENTION_PENDING:
                    chip.setText("Convention");
                    chip.setChipBackgroundColorResource(R.color.status_pending);
                    chip.setTextColor(context.getColor(R.color.text_primary));
                    break;
                case IN_PROGRESS:
                    chip.setText("En cours");
                    chip.setChipBackgroundColorResource(R.color.status_planned);
                    chip.setTextColor(context.getColor(R.color.white));
                    break;
                case CLOSED:
                    chip.setText("Clôturé");
                    chip.setChipBackgroundColorResource(R.color.status_planned);
                    chip.setTextColor(context.getColor(R.color.white));
                    break;
            }
        }
    }
}