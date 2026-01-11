package ma.ensate.pfa_manager.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.dto.PFAWithSoutenance;

public class PFASoutenanceAdapter extends ListAdapter<PFAWithSoutenance, PFASoutenanceAdapter.ViewHolder> {

    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.FRENCH);

    public interface OnItemClickListener {
        void onPlanClick(PFAWithSoutenance item);
        void onEditClick(PFAWithSoutenance item);
        void onDeleteClick(PFAWithSoutenance item);
    }

    public PFASoutenanceAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PFAWithSoutenance> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PFAWithSoutenance>() {
                @Override
                public boolean areItemsTheSame(@NonNull PFAWithSoutenance o, @NonNull PFAWithSoutenance n) {
                    return o.pfa.getPfa_id().equals(n.pfa.getPfa_id());
                }

                @Override
                public boolean areContentsTheSame(@NonNull PFAWithSoutenance o, @NonNull PFAWithSoutenance n) {
                    boolean sameSoutenance = (o.soutenance == null && n.soutenance == null) ||
                            (o.soutenance != null && n.soutenance != null &&
                                    o.soutenance.getDate_soutenance().equals(n.soutenance.getDate_soutenance()) &&
                                    o.soutenance.getLocation().equals(n.soutenance.getLocation()));
                    return o.pfa.getTitle().equals(n.pfa.getTitle()) && sameSoutenance;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pfa_soutenance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvTitle, tvStudent, tvDateInfo, tvLocationInfo;
        private final Chip chipStatus;
        private final LinearLayout layoutPlanned, layoutNotPlanned;
        private final MaterialButton btnPlan, btnEdit;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStudent = itemView.findViewById(R.id.tvStudent);
            chipStatus = itemView.findViewById(R.id.chipStatus);

            layoutPlanned = itemView.findViewById(R.id.layoutPlanned);
            layoutNotPlanned = itemView.findViewById(R.id.layoutNotPlanned);

            tvDateInfo = itemView.findViewById(R.id.tvDateInfo);
            tvLocationInfo = itemView.findViewById(R.id.tvLocationInfo);

            btnPlan = itemView.findViewById(R.id.btnPlan);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(PFAWithSoutenance item) {
            tvTitle.setText(item.pfa.getTitle());
            tvStudent.setText("Étudiant: " + item.pfa.getStudent_id()); // Remplacer par nom réel

            if (item.isPlanned()) {
                chipStatus.setText("Planifié");
                chipStatus.setChipBackgroundColorResource(R.color.status_planned);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));

                layoutPlanned.setVisibility(View.VISIBLE);
                layoutNotPlanned.setVisibility(View.GONE);

                tvDateInfo.setText(dateFormat.format(new Date(item.soutenance.getDate_soutenance())));
                tvLocationInfo.setText(item.soutenance.getLocation());

                btnEdit.setOnClickListener(v -> listener.onEditClick(item));
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));

                cardView.setStrokeColor(itemView.getContext().getColor(R.color.status_planned));
            } else {
                // Non planifié
                chipStatus.setText("Non planifié");
                chipStatus.setChipBackgroundColorResource(R.color.status_pending);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.text_dark));

                layoutPlanned.setVisibility(View.GONE);
                layoutNotPlanned.setVisibility(View.VISIBLE);

                btnPlan.setOnClickListener(v -> listener.onPlanClick(item));

                cardView.setStrokeColor(itemView.getContext().getColor(R.color.status_pending));
            }
        }
    }
}