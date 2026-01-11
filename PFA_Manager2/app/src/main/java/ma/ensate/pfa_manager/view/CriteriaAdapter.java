package ma.ensate.pfa_manager.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.dto.CriteriaWithScore;

public class CriteriaAdapter extends RecyclerView.Adapter<CriteriaAdapter.ViewHolder> {

    private List<CriteriaWithScore> criteriaList = new ArrayList<>();
    private OnScoreChangeListener listener;

    public interface OnScoreChangeListener {
        void onScoreChanged();
    }

    public void setOnScoreChangeListener(OnScoreChangeListener listener) {
        this.listener = listener;
    }

    public void setCriteriaList(List<CriteriaWithScore> list) {
        this.criteriaList = list;
        notifyDataSetChanged();
    }

    public List<CriteriaWithScore> getCriteriaList() {
        return criteriaList;
    }

    public double calculateTotalScore() {
        double totalScore = 0.0;
        double totalWeight = 0.0;
        for (CriteriaWithScore cs : criteriaList) {
            totalScore += cs.score * cs.getWeight();
            totalWeight += cs.getWeight();
        }
        if (totalWeight > 0) {
            return totalScore / totalWeight;
        }
        return 0.0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_criteria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(criteriaList.get(position));
    }

    @Override
    public int getItemCount() {
        return criteriaList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCriteriaLabel, tvCriteriaDescription, tvWeight, tvScore;
        private final Slider sliderScore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCriteriaLabel = itemView.findViewById(R.id.tvCriteriaLabel);
            tvCriteriaDescription = itemView.findViewById(R.id.tvCriteriaDescription);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvScore = itemView.findViewById(R.id.tvScore);
            sliderScore = itemView.findViewById(R.id.sliderScore);
        }

        void bind(CriteriaWithScore item) {
            tvCriteriaLabel.setText(item.getLabel());
            tvCriteriaDescription.setText(item.getDescription());
            tvWeight.setText(String.format(Locale.FRENCH, "(%.0f%%)", item.getWeight() * 100));

            sliderScore.setValue(item.score.floatValue());
            tvScore.setText(String.format(Locale.FRENCH, "%.1f/20", item.score));

            sliderScore.addOnChangeListener((slider, value, fromUser) -> {
                if (fromUser) {
                    item.score = (double) value;
                    tvScore.setText(String.format(Locale.FRENCH, "%.1f/20", item.score));
                    if (listener != null) {
                        listener.onScoreChanged();
                    }
                }
            });
        }
    }
}