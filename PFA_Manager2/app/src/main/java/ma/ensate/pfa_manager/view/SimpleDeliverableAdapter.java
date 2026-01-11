package ma.ensate.pfa_manager.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Deliverable;

public class SimpleDeliverableAdapter extends RecyclerView.Adapter<SimpleDeliverableAdapter.ViewHolder> {

    private List<Deliverable> deliverables = new ArrayList<>();
    private OnDeliverableClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);

    public interface OnDeliverableClickListener {
        void onDeliverableClick(Deliverable deliverable);
    }

    public void setDeliverables(List<Deliverable> deliverables) {
        this.deliverables = deliverables != null ? deliverables : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnDeliverableClickListener(OnDeliverableClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deliverable_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(deliverables.get(position));
    }

    @Override
    public int getItemCount() {
        return deliverables.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFileTitle, tvUploadDate;
        private final ImageView btnOpen;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileTitle = itemView.findViewById(R.id.tvFileTitle);
            tvUploadDate = itemView.findViewById(R.id.tvUploadDate);
            btnOpen = itemView.findViewById(R.id.btnOpen);
        }

        void bind(Deliverable item) {
            tvFileTitle.setText(item.getFile_title());

            if (item.getUploaded_at() != null) {
                tvUploadDate.setText(dateFormat.format(new Date(item.getUploaded_at())));
            } else {
                tvUploadDate.setText("Date inconnue");
            }

            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onDeliverableClick(item);
                }
            };

            itemView.setOnClickListener(clickListener);
            btnOpen.setOnClickListener(clickListener);
        }
    }
}