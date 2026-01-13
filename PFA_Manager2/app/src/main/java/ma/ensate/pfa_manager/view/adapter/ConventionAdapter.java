package ma.ensate.pfa_manager.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Convention;

public class ConventionAdapter extends RecyclerView.Adapter<ConventionAdapter.ConventionViewHolder> {
    
    private List<Convention> conventions;
    private ConventionActionListener listener;
    private boolean isPendingView; // true for PENDING, false for UPLOADED
    
    public interface ConventionActionListener {
        void onApprove(Convention convention);
        void onReject(Convention convention);
        void onValidate(Convention convention);
        void onRejectUploaded(Convention convention);
        void onViewDetails(Convention convention);
    }
    
    public ConventionAdapter(List<Convention> conventions, ConventionActionListener listener, boolean isPendingView) {
        this.conventions = conventions;
        this.listener = listener;
        this.isPendingView = isPendingView;
    }
    
    public void updateData(List<Convention> newConventions) {
        this.conventions = newConventions;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ConventionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_convention, parent, false);
        return new ConventionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ConventionViewHolder holder, int position) {
        Convention convention = conventions.get(position);
        holder.bind(convention, listener, isPendingView);
    }
    
    @Override
    public int getItemCount() {
        return conventions != null ? conventions.size() : 0;
    }
    
    static class ConventionViewHolder extends RecyclerView.ViewHolder {
        private TextView companyName;
        private TextView studentInfo;
        private TextView startDate;
        private TextView endDate;
        private Button btnApprove;
        private Button btnReject;
        private Button btnValidate;
        private Button btnRejectUploaded;
        
        public ConventionViewHolder(@NonNull View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.tvCompanyName);
            studentInfo = itemView.findViewById(R.id.tvStudentInfo);
            startDate = itemView.findViewById(R.id.tvStartDate);
            endDate = itemView.findViewById(R.id.tvEndDate);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnValidate = itemView.findViewById(R.id.btnValidate);
            btnRejectUploaded = itemView.findViewById(R.id.btnRejectUploaded);
        }
        
        public void bind(Convention convention, ConventionActionListener listener, boolean isPendingView) {
            companyName.setText(itemView.getContext().getString(R.string.label_company) + ": " + convention.getCompany_name());
            studentInfo.setText(itemView.getContext().getString(R.string.label_supervisor) + ": " + convention.getCompany_supervisor_name());
            startDate.setText(itemView.getContext().getString(R.string.label_start) + ": " + formatDate(convention.getStart_date()));
            endDate.setText(itemView.getContext().getString(R.string.label_end) + ": " + formatDate(convention.getEnd_date()));
            
            if (isPendingView) {
                // Afficher les boutons pour les demandes PENDING
                btnApprove.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                btnValidate.setVisibility(View.GONE);
                btnRejectUploaded.setVisibility(View.GONE);
                
                btnApprove.setOnClickListener(v -> listener.onApprove(convention));
                btnReject.setOnClickListener(v -> listener.onReject(convention));
            } else {
                // Afficher les boutons pour les conventions UPLOADED
                btnApprove.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                btnValidate.setVisibility(View.VISIBLE);
                btnRejectUploaded.setVisibility(View.VISIBLE);
                
                btnValidate.setOnClickListener(v -> listener.onValidate(convention));
                btnRejectUploaded.setOnClickListener(v -> listener.onRejectUploaded(convention));
            }

            // ouvrir le détail quand on clique sur la carte
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(convention);
                }
            });
        }
        
        private String formatDate(Long timestamp) {
            if (timestamp == null) return itemView.getContext().getString(R.string.value_na);
            return new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(timestamp));
        }
    }
}
