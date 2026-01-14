package ma.ensate.pfa_manager.view.etudiant;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.DeliverableFileType;
import ma.ensate.pfa_manager.model.DeliverableType;
import ma.ensate.pfa_manager.model.User;

public class ViewDeliverablesActivity extends AppCompatActivity {
    
    private static final String TAG = "ViewDeliverablesActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private User currentUser;
    private Long pfaId;
    private AppDatabase database;
    
    // Avant Soutenance
    private TextView tvRapportAvancementStatus, tvRapportAvancementName, tvRapportAvancementDate;
    private TextView tvRapportFinalBeforeStatus, tvRapportFinalBeforeName, tvRapportFinalBeforeDate;
    private TextView tvPresentationBeforeStatus, tvPresentationBeforeName, tvPresentationBeforeDate;
    private LinearLayout layoutRapportAvancement, layoutRapportFinalBefore, layoutPresentationBefore;
    private MaterialButton btnDownloadRapportAvancement, btnDownloadRapportFinalBefore, btnDownloadPresentationBefore;
    
    // Après Soutenance
    private TextView tvRapportFinalAfterStatus, tvRapportFinalAfterName, tvRapportFinalAfterDate;
    private TextView tvPresentationAfterStatus, tvPresentationAfterName, tvPresentationAfterDate;
    private LinearLayout layoutRapportFinalAfter, layoutPresentationAfter;
    private MaterialButton btnDownloadRapportFinalAfter, btnDownloadPresentationAfter;
    
    private Deliverable rapportAvancement, rapportFinalBefore, presentationBefore;
    private Deliverable rapportFinalAfter, presentationAfter;
    
    // Variable temporaire pour stocker le livrable en attente de permission
    private Deliverable pendingDeliverable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deliverables);
        
        currentUser = (User) getIntent().getSerializableExtra("user");
        pfaId = getIntent().getLongExtra("pfa_id", -1L);
        
        if (currentUser == null || pfaId == -1L) {
            Toast.makeText(this, "Erreur: données manquantes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        database = AppDatabase.getInstance(getApplication());
        
        initViews();
        setupToolbar();
        loadDeliverables();
    }
    
    private void initViews() {
        // Avant Soutenance
        tvRapportAvancementStatus = findViewById(R.id.tvRapportAvancementStatus);
        tvRapportAvancementName = findViewById(R.id.tvRapportAvancementName);
        tvRapportAvancementDate = findViewById(R.id.tvRapportAvancementDate);
        layoutRapportAvancement = findViewById(R.id.layoutRapportAvancement);
        btnDownloadRapportAvancement = findViewById(R.id.btnDownloadRapportAvancement);
        
        tvRapportFinalBeforeStatus = findViewById(R.id.tvRapportFinalBeforeStatus);
        tvRapportFinalBeforeName = findViewById(R.id.tvRapportFinalBeforeName);
        tvRapportFinalBeforeDate = findViewById(R.id.tvRapportFinalBeforeDate);
        layoutRapportFinalBefore = findViewById(R.id.layoutRapportFinalBefore);
        btnDownloadRapportFinalBefore = findViewById(R.id.btnDownloadRapportFinalBefore);
        
        tvPresentationBeforeStatus = findViewById(R.id.tvPresentationBeforeStatus);
        tvPresentationBeforeName = findViewById(R.id.tvPresentationBeforeName);
        tvPresentationBeforeDate = findViewById(R.id.tvPresentationBeforeDate);
        layoutPresentationBefore = findViewById(R.id.layoutPresentationBefore);
        btnDownloadPresentationBefore = findViewById(R.id.btnDownloadPresentationBefore);
        
        // Après Soutenance
        tvRapportFinalAfterStatus = findViewById(R.id.tvRapportFinalAfterStatus);
        tvRapportFinalAfterName = findViewById(R.id.tvRapportFinalAfterName);
        tvRapportFinalAfterDate = findViewById(R.id.tvRapportFinalAfterDate);
        layoutRapportFinalAfter = findViewById(R.id.layoutRapportFinalAfter);
        btnDownloadRapportFinalAfter = findViewById(R.id.btnDownloadRapportFinalAfter);
        
        tvPresentationAfterStatus = findViewById(R.id.tvPresentationAfterStatus);
        tvPresentationAfterName = findViewById(R.id.tvPresentationAfterName);
        tvPresentationAfterDate = findViewById(R.id.tvPresentationAfterDate);
        layoutPresentationAfter = findViewById(R.id.layoutPresentationAfter);
        btnDownloadPresentationAfter = findViewById(R.id.btnDownloadPresentationAfter);
    }
    
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.view_deliverables_title);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadDeliverables() {
        new Thread(() -> {
            List<Deliverable> deliverables = database.deliverableDao().getByPfaId(pfaId.longValue());
            
            for (Deliverable deliverable : deliverables) {
                DeliverableType type = deliverable.getDeliverable_type();
                DeliverableFileType fileType = deliverable.getDeliverable_file_type();
                
                if (type == DeliverableType.BEFORE_DEFENSE) {
                    if (fileType == DeliverableFileType.RAPPORT_AVANCEMENT) {
                        rapportAvancement = deliverable;
                    } else if (fileType == DeliverableFileType.RAPPORT_FINAL) {
                        rapportFinalBefore = deliverable;
                    } else if (fileType == DeliverableFileType.PRESENTATION) {
                        presentationBefore = deliverable;
                    }
                } else if (type == DeliverableType.AFTER_DEFENSE) {
                    if (fileType == DeliverableFileType.RAPPORT_FINAL) {
                        rapportFinalAfter = deliverable;
                    } else if (fileType == DeliverableFileType.PRESENTATION) {
                        presentationAfter = deliverable;
                    }
                }
            }
            
            runOnUiThread(this::updateUI);
        }).start();
    }
    
    private void updateUI() {
        // Rapport d'Avancement
        if (rapportAvancement != null) {
            displayDeliverable(rapportAvancement, tvRapportAvancementStatus, tvRapportAvancementName, 
                tvRapportAvancementDate, layoutRapportAvancement, btnDownloadRapportAvancement);
        }
        
        // Rapport Final Avant
        if (rapportFinalBefore != null) {
            displayDeliverable(rapportFinalBefore, tvRapportFinalBeforeStatus, tvRapportFinalBeforeName, 
                tvRapportFinalBeforeDate, layoutRapportFinalBefore, btnDownloadRapportFinalBefore);
        }
        
        // Présentation Avant
        if (presentationBefore != null) {
            displayDeliverable(presentationBefore, tvPresentationBeforeStatus, tvPresentationBeforeName, 
                tvPresentationBeforeDate, layoutPresentationBefore, btnDownloadPresentationBefore);
        }
        
        // Rapport Final Après
        if (rapportFinalAfter != null) {
            displayDeliverable(rapportFinalAfter, tvRapportFinalAfterStatus, tvRapportFinalAfterName, 
                tvRapportFinalAfterDate, layoutRapportFinalAfter, btnDownloadRapportFinalAfter);
        }
        
        // Présentation Après
        if (presentationAfter != null) {
            displayDeliverable(presentationAfter, tvPresentationAfterStatus, tvPresentationAfterName, 
                tvPresentationAfterDate, layoutPresentationAfter, btnDownloadPresentationAfter);
        }
    }
    
    private void displayDeliverable(Deliverable deliverable, TextView statusView, TextView nameView, 
                                     TextView dateView, LinearLayout layout, MaterialButton downloadBtn) {
        statusView.setText(R.string.deliverable_file_deposited);
        statusView.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess));
        
        nameView.setText("📎 " + deliverable.getFile_title());
        
        String dateStr = formatDate(deliverable.getUploaded_at());
        dateView.setText(getString(R.string.deliverable_deposited_on, dateStr));
        
        layout.setVisibility(View.VISIBLE);
        
        downloadBtn.setOnClickListener(v -> downloadDeliverable(deliverable));
    }
    
    private String formatDate(Long timestamp) {
        if (timestamp == null) return "Date inconnue";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE);
        return sdf.format(new Date(timestamp));
    }
    
    private void downloadDeliverable(Deliverable deliverable) {
        // Stocker le livrable pour l'utiliser dans onRequestPermissionsResult si permission demandée
        pendingDeliverable = deliverable;
        
        // Android 13+ (API 33) utilise READ_MEDIA_* permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                    PERMISSION_REQUEST_CODE);
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-12 utilise WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        // Si on est arrivé ici, on a la permission, on peut continuer
        proceedWithDownload(deliverable);
    }
    
    private void proceedWithDownload(Deliverable deliverable) {
        String fileUri = deliverable.getFile_uri();
        if (fileUri == null || fileUri.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_download_url_missing), Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Si c'est un chemin local (commence par /), on ouvre le fichier
            if (fileUri.startsWith("/")) {
                openLocalFile(new java.io.File(fileUri));
            } else if (fileUri.startsWith("http")) {
                // Si c'est une URL, on télécharge
                downloadFile(fileUri, deliverable.getFile_title());
            } else {
                // Sinon, on essaie de l'ouvrir comme URI content://
                openLocalFile(new java.io.File(fileUri));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du traitement du fichier", e);
            Toast.makeText(this, getString(R.string.error_download), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void downloadFile(String fileUrl, String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
            
            request.setTitle(fileName);
            request.setDescription("Téléchargement du livrable...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            
            downloadManager.enqueue(request);
            Toast.makeText(this, getString(R.string.download_started), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Téléchargement démarré pour: " + fileName);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du téléchargement", e);
            Toast.makeText(this, getString(R.string.error_download), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openLocalFile(java.io.File file) {
        try {
            if (!file.exists()) {
                Toast.makeText(this, "Fichier non trouvé: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Ouvrir le fichier avec l'application par défaut
            Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                this, 
                getPackageName() + ".fileprovider", 
                file
            );
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, getMimeType(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(intent);
            Log.d(TAG, "Ouverture du fichier: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'ouverture du fichier", e);
            Toast.makeText(this, "Impossible d'ouvrir le fichier", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getMimeType(java.io.File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) return "application/msword";
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".zip")) return "application/zip";
        return "*/*";
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, continuer avec le téléchargement du livrable en attente
                if (pendingDeliverable != null) {
                    proceedWithDownload(pendingDeliverable);
                    pendingDeliverable = null; // Réinitialiser après utilisation
                } else {
                    Toast.makeText(this, "Erreur: livrable non trouvé", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.permission_storage_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
