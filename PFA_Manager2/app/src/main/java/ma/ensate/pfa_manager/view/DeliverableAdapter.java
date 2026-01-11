package ma.ensate.pfa_manager.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import ma.ensate.pfa_manager.model.DeliverableFileType;
import ma.ensate.pfa_manager.model.DeliverableType;
import ma.ensate.pfa_manager.model.dto.DeliverableWithStudent;

public class DeliverableAdapter extends ListAdapter<DeliverableWithStudent, DeliverableAdapter.ViewHolder> {

    private final OnDeliverableClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.FRENCH);

    public interface OnDeliverableClickListener {
        void onOpenClick(DeliverableWithStudent item);
    }

    public DeliverableAdapter(OnDeliverableClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<DeliverableWithStudent> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DeliverableWithStudent>() {
                @Override
                public boolean areItemsTheSame(@NonNull DeliverableWithStudent oldItem, @NonNull DeliverableWithStudent newItem) {
                    return oldItem.getDeliverableId() != null &&
                            oldItem.getDeliverableId().equals(newItem.getDeliverableId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull DeliverableWithStudent oldItem, @NonNull DeliverableWithStudent newItem) {
                    return oldItem.getFileTitle().equals(newItem.getFileTitle()) &&
                            oldItem.getStudentFullName().equals(newItem.getStudentFullName());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deliverable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView ivFileIcon;
        private final TextView tvFileTitle, tvPfaTitle, tvStudentInitials, tvStudentName, tvUploadDate;
        private final Chip chipType, chipFileType;
        private final MaterialButton btnOpen;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            tvFileTitle = itemView.findViewById(R.id.tvFileTitle);
            tvPfaTitle = itemView.findViewById(R.id.tvPfaTitle);
            tvStudentInitials = itemView.findViewById(R.id.tvStudentInitials);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvUploadDate = itemView.findViewById(R.id.tvUploadDate);
            chipType = itemView.findViewById(R.id.chipType);
            chipFileType = itemView.findViewById(R.id.chipFileType);
            btnOpen = itemView.findViewById(R.id.btnOpen);
        }

        void bind(DeliverableWithStudent item) {
            Context context = itemView.getContext();

            tvFileTitle.setText(item.getFileTitle());
            tvPfaTitle.setText(item.getPfaTitle());
            tvStudentInitials.setText(item.getStudentInitials());
            tvStudentName.setText(item.getStudentFullName());

            if (item.getUploadedAt() != null) {
                tvUploadDate.setText("Soumis le " + dateFormat.format(new Date(item.getUploadedAt())));
            } else {
                tvUploadDate.setText("Date inconnue");
            }

            setDeliverableTypeChip(context, item.getDeliverableType());
            setFileTypeChip(context, item.getDeliverableFileType());
            setFileIcon(item.getFileUri());

            btnOpen.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenClick(item);
                }
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenClick(item);
                }
            });
        }

        private void setDeliverableTypeChip(Context context, DeliverableType type) {
            if (type == null) {
                chipType.setVisibility(View.GONE);
                return;
            }

            chipType.setVisibility(View.VISIBLE);
            switch (type) {
                case BEFORE_DEFENSE:
                    chipType.setText("Avant");
                    chipType.setChipBackgroundColorResource(R.color.primary_light);
                    chipType.setTextColor(context.getColor(R.color.primary));
                    break;
                case AFTER_DEFENSE:
                    chipType.setText("Après");
                    chipType.setChipBackgroundColorResource(R.color.status_planned);
                    chipType.setTextColor(context.getColor(R.color.white));
                    break;
            }
        }

        private void setFileTypeChip(Context context, DeliverableFileType fileType) {
            if (fileType == null) {
                chipFileType.setVisibility(View.GONE);
                return;
            }

            chipFileType.setVisibility(View.VISIBLE);
            switch (fileType) {
                case RAPPORT_AVANCEMENT:
                    chipFileType.setText("Rapport");
                    break;
                case PRESENTATION:
                    chipFileType.setText("Présentation");
                    break;
                case RAPPORT_FINAL:
                    chipFileType.setText("Final");
                    break;
            }
        }

        private void setFileIcon(String fileUri) {
            if (fileUri == null) {
                ivFileIcon.setImageResource(R.drawable.ic_document);
                return;
            }

            String lowerUri = fileUri.toLowerCase();
            if (lowerUri.endsWith(".pdf")) {
                ivFileIcon.setImageResource(R.drawable.ic_document);
            } else if (lowerUri.endsWith(".ppt") || lowerUri.endsWith(".pptx")) {
                ivFileIcon.setImageResource(R.drawable.ic_document);
            } else {
                ivFileIcon.setImageResource(R.drawable.ic_document);
            }
        }
    }
}