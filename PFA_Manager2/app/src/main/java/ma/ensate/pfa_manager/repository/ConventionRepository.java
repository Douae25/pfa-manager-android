package ma.ensate.pfa_manager.repository;

import android.app.Application;
import android.util.Log;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.ConventionDao;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.ConventionRequest;
import ma.ensate.pfa_manager.model.api.ConventionResponse;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;
import retrofit2.Call;
import retrofit2.Response;

public class ConventionRepository {
    
    private ConventionDao conventionDao;
    private ExecutorService executorService;
    private ApiService apiService;
    
    public ConventionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        conventionDao = database.conventionDao();
        apiService = ApiClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public void insert(Convention convention, OnConventionInsertedListener listener) {
        executorService.execute(() -> {
            long id = conventionDao.insert(convention);
            convention.setConvention_id(id);
            if (listener != null) {
                listener.onConventionInserted(convention);
            }
        });
    }
    
    public void update(Convention convention) {
        executorService.execute(() -> conventionDao.update(convention));
    }
    
    public void delete(Convention convention) {
        executorService.execute(() -> conventionDao.delete(convention));
    }
    
    public void getById(long id, OnConventionFetchedListener listener) {
        executorService.execute(() -> {
            Convention convention = conventionDao.getById(id);
            if (listener != null) {
                listener.onConventionFetched(convention);
            }
        });
    }
    
    public void getByPfaId(long pfaId, OnConventionFetchedListener listener) {
        executorService.execute(() -> {
            Convention convention = conventionDao.getByPfaId(pfaId);
            if (listener != null) {
                listener.onConventionFetched(convention);
            }
        });
    }
    
    public void getByState(ConventionState state, OnConventionsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Convention> conventions = conventionDao.getByState(state);
            if (listener != null) {
                listener.onConventionsListFetched(conventions);
            }
        });
    }
    
    public void getAll(OnConventionsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Convention> conventions = conventionDao.getAll();
            if (listener != null) {
                listener.onConventionsListFetched(conventions);
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    // STUDENT API: Request Convention (POST)
    // ════════════════════════════════════════════════════════════
    public void requestConvention(ConventionRequest request, OnConventionRequestListener listener) {
        executorService.execute(() -> {
            try {
                Log.d("ConventionAPI", "📤 Envoi requête: pfaId=" + request.getPfaId() + 
                    ", company=" + request.getCompanyName());
                
                // Appeler l'API
                Call<ConventionResponse> call = apiService.requestConvention(request);
                Response<ConventionResponse> response = call.execute();
                
                Log.d("ConventionAPI", "📥 Réponse reçue: code=" + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    ConventionResponse conventionResponse = response.body();
                    
                    // Convertir en objet Convention pour Room
                    Convention convention = new Convention();
                    convention.setPfa_id(conventionResponse.getPfaId());
                    convention.setCompany_name(conventionResponse.getCompanyName());
                    convention.setCompany_address(conventionResponse.getCompanyAddress());
                    convention.setCompany_supervisor_name(conventionResponse.getCompanySupervisorName());
                    convention.setCompany_supervisor_email(conventionResponse.getCompanySupervisorEmail());
                    convention.setStart_date(conventionResponse.getStartDate());
                    convention.setEnd_date(conventionResponse.getEndDate());
                    convention.setScanned_file_uri(conventionResponse.getScannedFileUri());
                    convention.setIs_validated(conventionResponse.getIsValidated());
                    convention.setState(ConventionState.valueOf(conventionResponse.getState()));
                    convention.setAdmin_comment(conventionResponse.getAdminComment());
                    convention.setIs_synced(true);  // Marquer comme synced
                    convention.setBackend_convention_id(conventionResponse.getConventionId());  // Sauvegarder l'ID backend
                    
                    // Sauvegarder en Room
                    long localId = conventionDao.insert(convention);
                    convention.setConvention_id(localId);  // Set local ID for callback
                    
                    if (listener != null) {
                        listener.onSuccess(convention);
                    }
                } else {
                    String errorMessage = "Erreur serveur: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        // Garder le message par défaut
                    }
                    if (listener != null) {
                        listener.onError(errorMessage);
                    }
                }
            } catch (Exception e) {
                // Offline mode - sauvegarder en attente de sync
                Convention convention = new Convention();
                convention.setPfa_id(request.getPfaId());
                convention.setCompany_name(request.getCompanyName());
                convention.setCompany_address(request.getCompanyAddress());
                convention.setCompany_supervisor_name(request.getCompanySupervisorName());
                convention.setCompany_supervisor_email(request.getCompanySupervisorEmail());
                convention.setStart_date(request.getStartDate());
                convention.setEnd_date(request.getEndDate());
                convention.setState(ConventionState.PENDING);
                
                long id = conventionDao.insert(convention);
                convention.setConvention_id(id);
                
                if (listener != null) {
                    listener.onOffline("Données sauvegardées localement. Elles seront synchronisées à la reconnexion.");
                }
            }
        });
    }
    
    public interface OnConventionInsertedListener {
        void onConventionInserted(Convention convention);
    }
    
    public interface OnConventionFetchedListener {
        void onConventionFetched(Convention convention);
    }
    
    public interface OnConventionsListFetchedListener {
        void onConventionsListFetched(List<Convention> conventions);
    }

    
    // ════════════════════════════════════════════════════════════
    // STUDENT API: Upload Signed Convention
    // ════════════════════════════════════════════════════════════
    public void uploadSignedConvention(Long conventionId, String scannedFileUri, 
                                        OnConventionUploadListener listener) {
        Log.d("ConventionAPI", "🚀 uploadSignedConvention() appelée: id=" + conventionId);
        executorService.execute(() -> {
            try {
                // Récupérer la convention pour obtenir le backend_convention_id
                Convention convention = conventionDao.getById(conventionId);
                if (convention == null) {
                    Log.e("ConventionAPI", "❌ Convention introuvable: id=" + conventionId);
                    if (listener != null) {
                        listener.onError("Convention introuvable");
                    }
                    return;
                }
                
                // Utiliser backend_convention_id si disponible, sinon utiliser l'ID local
                Long backendId = convention.getBackend_convention_id() != null ? 
                    convention.getBackend_convention_id() : conventionId;
                
                Log.d("ConventionAPI", "📤 Upload convention signée: backendId=" + backendId + 
                    ", uri=" + scannedFileUri);
                
                // Appeler l'API avec le backend ID
                Call<ConventionResponse> call = apiService.uploadSignedConvention(backendId, scannedFileUri);
                Response<ConventionResponse> response = call.execute();
                
                Log.d("ConventionAPI", "📥 Réponse upload: code=" + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    ConventionResponse conventionResponse = response.body();
                    
                    // Mettre à jour en Room
                    convention.setScanned_file_uri(conventionResponse.getScannedFileUri());
                    convention.setState(ConventionState.UPLOADED);
                    convention.setIs_validated(conventionResponse.getIsValidated());
                    convention.setAdmin_comment(conventionResponse.getAdminComment());
                    convention.setIs_synced(true);
                    conventionDao.update(convention);
                    
                    Log.d("ConventionAPI", "✅ Convention uploaded - state changé à UPLOADED");
                    if (listener != null) {
                        listener.onSuccess(convention);
                    }
                } else {
                    // Erreur backend (400, 404, etc.) - on sauvegarde quand même localement
                    String errorMessage = "Erreur upload: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        // Garder le message par défaut
                    }
                    Log.e("ConventionAPI", "❌ " + errorMessage);
                    
                    // Sauvegarder localement même si backend refuse
                    convention.setScanned_file_uri(scannedFileUri);
                    convention.setState(ConventionState.UPLOADED);
                    conventionDao.update(convention);
                    Log.d("ConventionAPI", "📱 Fichier sauvegardé localement avec state=UPLOADED (erreur backend)");
                    
                    if (listener != null) {
                        listener.onError(errorMessage);
                    }
                }
            } catch (Exception e) {
                Log.e("ConventionAPI", "❌ Erreur upload exception: " + e.getMessage(), e);
                // Offline mode - marquer comme "pending upload"
                try {
                    Convention convention = conventionDao.getById(conventionId);
                    if (convention != null) {
                        convention.setScanned_file_uri(scannedFileUri);  // Sauvegarder localement
                        convention.setState(ConventionState.UPLOADED);
                        convention.setIs_synced(false);  // Marquer comme pending sync
                        conventionDao.update(convention);
                        
                        Log.d("ConventionAPI", "📱 Convention sauvegardée localement avec state=UPLOADED (offline)");
                        if (listener != null) {
                            listener.onOffline("Convention sauvegardée localement. Upload synchronisé à la reconnexion.");
                        }
                    } else {
                        Log.e("ConventionAPI", "❌ Convention null, impossible de sauvegarder");
                    }
                } catch (Exception ex) {
                    Log.e("ConventionAPI", "❌ Erreur sauvegarde offline: " + ex.getMessage(), ex);
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    // STUDENT API: Get Convention by ID (consultation)
    // ════════════════════════════════════════════════════════════
    public void getConventionById(Long conventionId, OnGetConventionListener listener) {
        executorService.execute(() -> {
            try {
                Call<ConventionResponse> call = apiService.getConventionById(conventionId);
                Response<ConventionResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ConventionResponse conventionResponse = response.body();
                    
                    // Mettre à jour en Room
                    Convention convention = conventionDao.getById(conventionId);
                    if (convention != null) {
                        convention.setScanned_file_uri(conventionResponse.getScannedFileUri());
                        convention.setState(ConventionState.valueOf(conventionResponse.getState()));
                        convention.setIs_validated(conventionResponse.getIsValidated());
                        convention.setAdmin_comment(conventionResponse.getAdminComment());
                        conventionDao.update(convention);
                    }
                    
                    if (listener != null) {
                        listener.onSuccess(convention);
                    }
                } else {
                    if (listener != null) {
                        listener.onError("Erreur: " + response.code());
                    }
                }
            } catch (Exception e) {
                // Offline: retourner depuis Room
                Convention convention = conventionDao.getById(conventionId);
                if (listener != null) {
                    listener.onSuccess(convention);
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    // LISTENER INTERFACES
    // ════════════════════════════════════════════════════════════
    public interface OnConventionUploadListener {
        void onSuccess(Convention convention);
        void onError(String error);
        void onOffline(String message);
    }

    public interface OnGetConventionListener {
        void onSuccess(Convention convention);
        void onError(String error);
    }

    public interface OnConventionRequestListener {
        void onSuccess(Convention convention);
        void onError(String error);
        void onOffline(String message);
    }}