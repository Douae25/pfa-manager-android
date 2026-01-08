package ma.ensate.pfa_manager.view.etudiant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.DeliverableFile;

public class DeliverableFilesAdapter extends RecyclerView.Adapter<DeliverableFilesAdapter.ViewHolder> {
    
    private List<DeliverableFile> files;
    private OnFileDeleteListener deleteListener;
    
    public interface OnFileDeleteListener {
        void onFileDelete(int position);
    }
    
    public DeliverableFilesAdapter(List<DeliverableFile> files, OnFileDeleteListener deleteListener) {
        this.files = files;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deliverable_file, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeliverableFile file = files.get(position);
        holder.tvFileName.setText(file.getFileName());
        holder.tvFileSize.setText(formatFileSize(file.getFileSize()));
        holder.ibDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onFileDelete(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return files.size();
    }
    
    private String formatFileSize(long sizeBytes) {
        if (sizeBytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(sizeBytes) / Math.log10(1024));
        return String.format("%.1f %s", sizeBytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        TextView tvFileSize;
        ImageButton ibDelete;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            ibDelete = itemView.findViewById(R.id.ibDelete);
        }
    }
}
