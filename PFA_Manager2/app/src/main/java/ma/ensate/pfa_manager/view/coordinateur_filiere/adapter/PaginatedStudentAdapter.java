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

public class PaginatedStudentAdapter extends RecyclerView.Adapter<PaginatedStudentAdapter.ElegantStudentViewHolder> {

    private List<StudentWithEvaluation> allStudents = new ArrayList<>();
    private List<StudentWithEvaluation> currentPageStudents = new ArrayList<>();
    private int currentPage = 1;
    private int pageSize = 4;
    private OnPaginationChangeListener listener;

    @NonNull
    @Override
    public ElegantStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new ElegantStudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ElegantStudentViewHolder holder, int position) {
        StudentWithEvaluation student = currentPageStudents.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return currentPageStudents.size();
    }

    public void setAllStudents(List<StudentWithEvaluation> students) {
        this.allStudents = students;
        updateCurrentPage();
        if (listener != null) {
            listener.onPaginationChanged(currentPage, getTotalPages(), getTotalStudents());
        }
    }

    public int getTotalPages() {
        if (allStudents.isEmpty()) return 1;
        return (int) Math.ceil((double) allStudents.size() / pageSize);
    }

    public int getTotalStudents() {
        return allStudents.size();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void nextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            updateCurrentPage();
            if (listener != null) {
                listener.onPaginationChanged(currentPage, getTotalPages(), getTotalStudents());
            }
        }
    }

    public void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            updateCurrentPage();
            if (listener != null) {
                listener.onPaginationChanged(currentPage, getTotalPages(), getTotalStudents());
            }
        }
    }

    public boolean hasNextPage() {
        return currentPage < getTotalPages();
    }

    public boolean hasPrevPage() {
        return currentPage > 1;
    }

    private void updateCurrentPage() {
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allStudents.size());

        currentPageStudents.clear();
        if (startIndex < allStudents.size()) {
            currentPageStudents.addAll(allStudents.subList(startIndex, endIndex));
        }

        notifyDataSetChanged();
    }

    public void setOnPaginationChangeListener(OnPaginationChangeListener listener) {
        this.listener = listener;
    }

    public interface OnPaginationChangeListener {
        void onPaginationChanged(int currentPage, int totalPages, int totalStudents);
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
                //Ouvrir les détails complets
            });
        }
    }
}