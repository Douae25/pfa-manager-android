package ma.ensate.pfa_manager.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import ma.ensate.pfa_manager.model.dto.SoutenanceWithEvaluation;

public class EvaluationAdapter extends ListAdapter<SoutenanceWithEvaluation, EvaluationAdapter.ViewHolder> {

    private final OnEvaluationClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.FRENCH);

    public interface OnEvaluationClickListener {
        void onEvaluateClick(SoutenanceWithEvaluation item);
    }

    public EvaluationAdapter(OnEvaluationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SoutenanceWithEvaluation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SoutenanceWithEvaluation>() {
                @Override
                public boolean areItemsTheSame(@NonNull SoutenanceWithEvaluation oldItem,
                                               @NonNull SoutenanceWithEvaluation newItem) {
                    return oldItem.getSoutenanceId() != null &&
                            oldItem.getSoutenanceId().equals(newItem.getSoutenanceId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull SoutenanceWithEvaluation oldItem,
                                                  @NonNull SoutenanceWithEvaluation newItem) {
                    return oldItem.isEvaluated() == newItem.isEvaluated() &&
                            oldItem.getPfaTitle().equals(newItem.getPfaTitle());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evaluation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvPfaTitle, tvStudentInitials, tvStudentName;
        private final TextView tvDate, tvLocation, tvScore;
        private final Chip chipStatus;
        private final LinearLayout layoutScore;
        private final MaterialButton btnEvaluate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvPfaTitle = itemView.findViewById(R.id.tvPfaTitle);
            tvStudentInitials = itemView.findViewById(R.id.tvStudentInitials);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvScore = itemView.findViewById(R.id.tvScore);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            layoutScore = itemView.findViewById(R.id.layoutScore);
            btnEvaluate = itemView.findViewById(R.id.btnEvaluate);
        }

        void bind(SoutenanceWithEvaluation item) {
            Context context = itemView.getContext();

            tvPfaTitle.setText(item.getPfaTitle());
            tvStudentInitials.setText(item.getStudentInitials());
            tvStudentName.setText(item.getStudentFullName());

            if (item.getDateSoutenance() != null) {
                tvDate.setText(dateFormat.format(new Date(item.getDateSoutenance())));
            }
            tvLocation.setText(item.getLocation());

            if (item.isEvaluated()) {
                chipStatus.setText("Évalué");
                chipStatus.setChipBackgroundColorResource(R.color.status_planned);
                chipStatus.setTextColor(context.getColor(R.color.white));

                layoutScore.setVisibility(View.VISIBLE);
                tvScore.setText(String.format(Locale.FRENCH, "%.1f/20", item.getTotalScore()));

                btnEvaluate.setVisibility(View.GONE);

                cardView.setStrokeColor(context.getColor(R.color.status_planned));
            } else {
                chipStatus.setText("À évaluer");
                chipStatus.setChipBackgroundColorResource(R.color.status_pending);
                chipStatus.setTextColor(context.getColor(R.color.text_primary));

                layoutScore.setVisibility(View.GONE);

                btnEvaluate.setVisibility(View.VISIBLE);
                btnEvaluate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEvaluateClick(item);
                    }
                });

                cardView.setStrokeColor(context.getColor(R.color.divider));
            }
        }
    }
}