package ma.ensate.pfa_manager.view.coordinateur_filiere.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.User;

public class ProfessorAssignmentAdapter extends RecyclerView.Adapter<ProfessorAssignmentAdapter.ViewHolder> {

    private List<ProfessorAssignment> assignments = new ArrayList<>();

    public static class ProfessorAssignment {
        private User professor;
        private List<User> students;

        public ProfessorAssignment(User professor, List<User> students) {
            this.professor = professor;
            this.students = students;
        }

        public User getProfessor() {
            return professor;
        }

        public List<User> getStudents() {
            return students;
        }
    }

    public void setAssignments(List<ProfessorAssignment> assignments) {
        this.assignments = assignments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_professor_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfessorAssignment assignment = assignments.get(position);
        holder.bind(assignment);
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProfessorName;
        private TextView tvProfessorInitials;
        private TextView tvStudentCount;
        private RecyclerView recyclerStudents;
        private AssignedStudentAdapter studentAdapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProfessorName = itemView.findViewById(R.id.tvProfessorName);
            tvProfessorInitials = itemView.findViewById(R.id.tvProfessorInitials);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
            recyclerStudents = itemView.findViewById(R.id.recyclerStudents);

            recyclerStudents.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            studentAdapter = new AssignedStudentAdapter();
            recyclerStudents.setAdapter(studentAdapter);
        }

        public void bind(ProfessorAssignment assignment) {
            User professor = assignment.getProfessor();
            List<User> students = assignment.getStudents();

            String fullName = professor.getFirst_name() + " " + professor.getLast_name();
            tvProfessorName.setText(fullName);

            // Initiales
            String initials = "";
            if (professor.getFirst_name() != null && !professor.getFirst_name().isEmpty()) {
                initials += professor.getFirst_name().charAt(0);
            }
            if (professor.getLast_name() != null && !professor.getLast_name().isEmpty()) {
                initials += professor.getLast_name().charAt(0);
            }
            tvProfessorInitials.setText(initials);

            tvStudentCount.setText(students.size() + " étudiant(s)");

            studentAdapter.setStudents(students);

            if (students.isEmpty()) {
                recyclerStudents.setVisibility(View.GONE);
            } else {
                recyclerStudents.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class AssignedStudentAdapter extends RecyclerView.Adapter<AssignedStudentAdapter.StudentViewHolder> {

        private List<User> students = new ArrayList<>();

        public void setStudents(List<User> students) {
            this.students = students;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_assigned_student, parent, false);
            return new StudentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            User student = students.get(position);
            holder.bind(student, position + 1);
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        static class StudentViewHolder extends RecyclerView.ViewHolder {
            private TextView tvStudentNumber;
            private TextView tvStudentName;
            private TextView tvStudentEmail;

            public StudentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStudentNumber = itemView.findViewById(R.id.tvStudentNumber);
                tvStudentName = itemView.findViewById(R.id.tvStudentName);
                tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            }

            public void bind(User student, int number) {
                tvStudentNumber.setText(String.valueOf(number));

                String fullName = student.getFirst_name() + " " + student.getLast_name();
                tvStudentName.setText(fullName);

                tvStudentEmail.setText(student.getEmail());
            }
        }
    }
}