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

public class PaginatedProfessorAdapter extends RecyclerView.Adapter<PaginatedProfessorAdapter.ElegantProfessorViewHolder> {

    private List<ProfessorWithStats> allProfessors = new ArrayList<>();
    private List<ProfessorWithStats> currentPageProfessors = new ArrayList<>();
    private int currentPage = 1;
    private int pageSize = 4; // 4 encadrants par page
    private OnPaginationChangeListener listener;

    @NonNull
    @Override
    public ElegantProfessorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_professor, parent, false);
        return new ElegantProfessorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ElegantProfessorViewHolder holder, int position) {
        ProfessorWithStats professor = currentPageProfessors.get(position);
        holder.bind(professor);
    }

    @Override
    public int getItemCount() {
        return currentPageProfessors.size();
    }

    public void setAllProfessors(List<ProfessorWithStats> professors) {
        this.allProfessors = professors;
        updateCurrentPage();
        if (listener != null) {
            listener.onPaginationChanged(currentPage, getTotalPages(), getTotalProfessors());
        }
    }

    public int getTotalPages() {
        if (allProfessors.isEmpty()) return 1;
        return (int) Math.ceil((double) allProfessors.size() / pageSize);
    }

    public int getTotalProfessors() {
        return allProfessors.size();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void nextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            updateCurrentPage();
            if (listener != null) {
                listener.onPaginationChanged(currentPage, getTotalPages(), getTotalProfessors());
            }
        }
    }

    public void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            updateCurrentPage();
            if (listener != null) {
                listener.onPaginationChanged(currentPage, getTotalPages(), getTotalProfessors());
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
        int endIndex = Math.min(startIndex + pageSize, allProfessors.size());

        currentPageProfessors.clear();
        if (startIndex < allProfessors.size()) {
            currentPageProfessors.addAll(allProfessors.subList(startIndex, endIndex));
        }

        notifyDataSetChanged();
    }

    public void setOnPaginationChangeListener(OnPaginationChangeListener listener) {
        this.listener = listener;
    }

    public interface OnPaginationChangeListener {
        void onPaginationChanged(int currentPage, int totalPages, int totalProfessors);
    }

    static class ElegantProfessorViewHolder extends RecyclerView.ViewHolder {
        private final TextView textAvatar;
        private final TextView textProfessorName;
        private final TextView textProfessorEmail;
        private final TextView textProfessorSpecialty;
        private final TextView textStudentsCount;
        private final TextView textAverageScore; // SUPPRIMÉ: textProjectsCount
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
            // Avatar
            textAvatar.setText(professor.getInitials());

            // Nom et email
            textProfessorName.setText(professor.getFullName());
            textProfessorEmail.setText(professor.getProfessor().getEmail());

            // Spécialité
            textProfessorSpecialty.setText("Encadrant PFA");

            // Statistiques
            textStudentsCount.setText(String.valueOf(professor.getStudentCount()));
            textAverageScore.setText(professor.getAverageScoreText());

            // Téléphone
            String phone = professor.getProfessor().getPhone_number();
            textProfessorPhone.setText(phone != null ? phone : "Non renseigné");

            // Click listener
            itemView.setOnClickListener(v -> {
                // TODO: Ouvrir les détails complets
            });
        }
    }
}