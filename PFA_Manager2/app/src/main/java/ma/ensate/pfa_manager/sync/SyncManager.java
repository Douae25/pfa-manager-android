package ma.ensate.pfa_manager.sync;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.ConventionDao;
import ma.ensate.pfa_manager.database.DeliverableDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;
import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.DeliverableFileType;
import ma.ensate.pfa_manager.model.DeliverableType;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.Department;
import ma.ensate.pfa_manager.model.api.ConventionRequest;
import ma.ensate.pfa_manager.model.api.ConventionResponse;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.DeliverableRequest;
import ma.ensate.pfa_manager.model.api.DeliverableResponse;
import ma.ensate.pfa_manager.model.api.EvaluationResponse;
import ma.ensate.pfa_manager.model.api.PFADossierRequest;
import ma.ensate.pfa_manager.model.api.PFADossierResponse;
import ma.ensate.pfa_manager.model.api.SoutenanceResponse;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;
import ma.ensate.pfa_manager.network.UserApi;
import ma.ensate.pfa_manager.network.DepartmentApi;
import ma.ensate.pfa_manager.network.PFADossierApi;
import ma.ensate.pfa_manager.network.ConventionApi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    
    private Application application;
    private AppDatabase database;
    private ApiService apiService;
    private ExecutorService executorService;
    private ConnectivityManager connectivityManager;
    
    private MutableLiveData<SyncStatus> syncStatus = new MutableLiveData<>(SyncStatus.IDLE);
    private MutableLiveData<String> syncMessage = new MutableLiveData<>();
    
    private boolean isSyncing = false;  // Verrou pour éviter les syncs parallèles
    private static final long SYNC_DEBOUNCE_MS = 1000;  // Débounce: ignorer les changements réseau < 1s
    
    public enum SyncStatus {
        IDLE, SYNCING, SUCCESS, ERROR
    }
    
    private SyncManager(Application application) {
        this.application = application;
        this.database = AppDatabase.getInstance(application);
        this.apiService = ApiClient.getApiService();
        this.executorService = Executors.newSingleThreadExecutor();
        this.connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        setupNetworkListener();
    }
    
    public static synchronized SyncManager getInstance(Application application) {
        if (instance == null) {
            instance = new SyncManager(application);
        }
        return instance;
    }
    
    // ════════════════════════════════════════════════════════════
    // BroadcastReceiver: Déclenche sync à CHAQUE changement réseau
    // ════════════════════════════════════════════════════════════
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (isNetworkAvailable()) {
                    Log.d(TAG, "🔗 CONNEXION DÉTECTÉE - Synchronisation en cours...");
                    // Éviter les syncs parallèles: si déjà en cours, ignorer cet appel
                    if (!isSyncing) {
                        syncPendingData();
                    } else {
                        Log.d(TAG, "⏳ Sync déjà en cours, ignorant cet événement réseau");
                    }
                } else {
                    Log.d(TAG, "🔌 Connexion perdue");
                    syncStatus.postValue(SyncStatus.IDLE);
                }
            }
        }
    };
    
    private void setupNetworkListener() {
        try {
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            application.registerReceiver(networkReceiver, intentFilter, Context.RECEIVER_EXPORTED);
            Log.d(TAG, "✅ BroadcastReceiver enregistré pour les changements réseau");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors de l'enregistrement du BroadcastReceiver", e);
        }
    }
    
    // ════════════════════════════════════════════════════════════
    // Synchroniser une convention spécifique
    // ════════════════════════════════════════════════════════════
    private boolean syncConvention(Convention convention) {
        try {
            Log.d(TAG, "📤 Envoi convention: " + convention.getConvention_id());

            // Récupérer le dossier PFA associé
            PFADossierDao pfaDossierDao = database.pfaDossierDao();
            PFADossier pfaDossier = pfaDossierDao.getById(convention.getPfa_id());

            // S'assurer que l'étudiant existe
            Long studentId = pfaDossier != null ? pfaDossier.getStudent_id() : getStudentIdForConvention(convention.getPfa_id());

            // 1) Créer/récupérer le dossier PFA côté backend si pas encore synchronisé
            Long backendPfaId = pfaDossier != null ? pfaDossier.getBackend_pfa_id() : null;
            if (backendPfaId == null) {
                PFADossierRequest pfaRequest = new PFADossierRequest();
                pfaRequest.setStudentId(studentId);
                pfaRequest.setTitle(pfaDossier != null ? pfaDossier.getTitle() : null);
                pfaRequest.setDescription(pfaDossier != null ? pfaDossier.getDescription() : null);
                pfaRequest.setSupervisorId(pfaDossier != null ? pfaDossier.getSupervisor_id() : null);

                Call<PFADossierResponse> pfaCall = apiService.createOrGetPFADossier(pfaRequest);
                Response<PFADossierResponse> pfaResponse = pfaCall.execute();

                if (pfaResponse.isSuccessful() && pfaResponse.body() != null) {
                    backendPfaId = pfaResponse.body().getPfaId();

                    // Mettre à jour le dossier local avec l'ID backend
                    if (pfaDossier != null) {
                        pfaDossier.setBackend_pfa_id(backendPfaId);
                        pfaDossier.setIs_synced(true);
                        pfaDossierDao.update(pfaDossier);
                    }
                    Log.d(TAG, "✅ Dossier PFA synchronisé (backendId=" + backendPfaId + ")");
                } else {
                    Log.e(TAG, "❌ Erreur API PFA dossier: " + pfaResponse.code());
                    return false; // On ne peut pas continuer sans dossier backend
                }
            }

            // 2) Créer la requête de convention en utilisant l'ID backend du PFA
            ConventionRequest request = new ConventionRequest();
            request.setStudentId(studentId);
            request.setPfaId(backendPfaId != null ? backendPfaId : convention.getPfa_id());
            request.setCompanyName(convention.getCompany_name());
            request.setCompanyAddress(convention.getCompany_address());
            request.setCompanySupervisorName(convention.getCompany_supervisor_name());
            request.setCompanySupervisorEmail(convention.getCompany_supervisor_email());
            request.setStartDate(convention.getStart_date());
            request.setEndDate(convention.getEnd_date());

            // Appeler l'API
            Call<ConventionResponse> call = apiService.requestConvention(request);
            Response<ConventionResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                ConventionResponse conventionResponse = response.body();

                if (conventionResponse != null) {
                    // Mettre à jour la convention en Room avec l'ID du serveur et l'état
                    convention.setBackend_convention_id(conventionResponse.getConventionId());  // Sauvegarder backend ID
                    convention.setIs_synced(true);  // Marquer comme synced
                    convention.setState(ConventionState.valueOf(conventionResponse.getState()));
                    convention.setIs_validated(conventionResponse.getIsValidated());
                    convention.setAdmin_comment(conventionResponse.getAdminComment());

                    // Garder la référence locale au PFA, mais on a le backend ID en PFADossier
                    ConventionDao conventionDao = database.conventionDao();
                    conventionDao.update(convention);

                    Log.d(TAG, "✅ Convention synchronisée: backendId=" + conventionResponse.getConventionId() + 
                        ", is_synced=true");
                    return true;
                } else {
                    Log.e(TAG, "❌ Erreur: ConventionResponse null");
                    return false;
                }
            } else if (response.code() == 400) {
                // Cas spécial: si la convention existe déjà, on marque comme synced
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                if (errorBody.contains("Convention already exists")) {
                    Log.d(TAG, "✅ Convention déjà existante sur le backend - marquée comme synced");
                    convention.setIs_synced(true);  // Marquer comme synced
                    ConventionDao conventionDao = database.conventionDao();
                    conventionDao.update(convention);
                    return true;
                }
                Log.e(TAG, "❌ Erreur API 400: " + errorBody);
                return false;
            } else {
                Log.e(TAG, "❌ Erreur API: " + response.code());
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception lors de la sync: " + e.getMessage(), e);
            return false;
        }
    }
    
    // ════════════════════════════════════════════════════════════
    // Synchroniser les uploads de conventions signées en attente
    // ════════════════════════════════════════════════════════════
    private void syncPendingUploads(ConventionDao conventionDao) {
        try {
            // Récupérer toutes les conventions
            List<Convention> allConventions = conventionDao.getAll();
            
            if (allConventions == null || allConventions.isEmpty()) {
                Log.d(TAG, "✅ Aucun upload en attente");
                return;
            }
            
            // Identifier celles avec un fichier scanné mais pas encore synced
            // is_synced=false = upload en attente d'envoi au backend
            int uploadCount = 0;
            for (Convention convention : allConventions) {
                // Si elle a un URI et is_synced=false, c'est un upload en attente de sync
                if (convention.getScanned_file_uri() != null && 
                    !convention.getScanned_file_uri().isEmpty() &&
                    !convention.isIs_synced()) {
                    
                    // Utiliser backend_convention_id si disponible, sinon local ID
                    Long backendId = convention.getBackend_convention_id() != null ? 
                        convention.getBackend_convention_id() : convention.getConvention_id();
                    
                    Log.d(TAG, "📤 Upload en attente: convention local=" + convention.getConvention_id() + 
                        ", backend=" + backendId);
                    uploadCount++;
                    
                    try {
                        // Appeler l'API pour uploader avec backend ID
                        Call<ConventionResponse> call = apiService.uploadSignedConvention(
                            backendId, 
                            convention.getScanned_file_uri()
                        );
                        Response<ConventionResponse> response = call.execute();
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ConventionResponse uploadedConvention = response.body();
                            
                            // Mettre à jour la convention avec la réponse du backend
                            convention.setState(ConventionState.UPLOADED);
                            convention.setIs_synced(true);  // Marquer comme synced
                            convention.setIs_validated(uploadedConvention.getIsValidated());
                            convention.setAdmin_comment(uploadedConvention.getAdminComment());
                            conventionDao.update(convention);
                            
                            Log.d(TAG, "✅ Upload réussi: convention " + convention.getConvention_id() + 
                                " → état: UPLOADED, is_synced=true");
                        } else {
                            Log.e(TAG, "❌ Erreur upload API: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Exception upload: " + e.getMessage());
                    }
                }
            }
            
            if (uploadCount > 0) {
                Log.d(TAG, "📤 " + uploadCount + " upload(s) synchronisé(s)");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur syncPendingUploads: " + e.getMessage());
        }
    }
    
    // ════════════════════════════════════════════════════════════
    // Synchroniser les livrables en attente (is_synced=false)
    // ════════════════════════════════════════════════════════════
    private void syncPendingDeliverables() {
        try {
            DeliverableDao deliverableDao = database.deliverableDao();
            List<Deliverable> allDeliverables = deliverableDao.getAll();
            
            if (allDeliverables == null || allDeliverables.isEmpty()) {
                Log.d(TAG, "✅ Aucun livrable en attente de sync");
                return;
            }
            
            int uploadCount = 0;
            for (Deliverable deliverable : allDeliverables) {
                // Si is_synced=false, c'est un livrable en attente de sync
                if (!deliverable.isIs_synced()) {
                    Log.d(TAG, "📤 Livrable en attente: id=" + deliverable.getDeliverable_id());
                    uploadCount++;
                    
                    try {
                        // Récupérer le PFADossier pour obtenir le backend_pfa_id
                        PFADossierDao pfaDossierDao = database.pfaDossierDao();
                        PFADossier pfaDossier = pfaDossierDao.getById(deliverable.getPfa_id());
                        
                        if (pfaDossier == null) {
                            Log.e(TAG, "❌ PFADossier introuvable pour deliverable: " + deliverable.getDeliverable_id());
                            continue;
                        }
                        
                        // Utiliser backend_pfa_id si disponible, sinon local pfa_id
                        Long backendPfaId = pfaDossier.getBackend_pfa_id() != null ? 
                            pfaDossier.getBackend_pfa_id() : deliverable.getPfa_id();
                        
                        // Créer la requête pour uploader le livrable
                        DeliverableRequest request = new DeliverableRequest();
                        request.setPfaId(backendPfaId);
                        request.setFileTitle(deliverable.getFile_title());
                        request.setFilePath(deliverable.getFile_uri());
                        request.setFileType(deliverable.getDeliverable_file_type());
                        request.setDeliverableType(deliverable.getDeliverable_type());
                        
                        Log.d(TAG, "📤 Upload livrable: localPfaId=" + deliverable.getPfa_id() + 
                            ", backendPfaId=" + backendPfaId);
                        
                        // Appeler l'API pour uploader
                        Call<DeliverableResponse> call = apiService.depositDeliverable(request);
                        Response<DeliverableResponse> response = call.execute();
                        
                        if (response.isSuccessful() && response.body() != null) {
                            DeliverableResponse uploadedDeliverable = response.body();
                            
                            // Mettre à jour la livrable en Room
                            deliverable.setIs_synced(true);
                            deliverable.setBackend_deliverable_id(uploadedDeliverable.getDeliverableId());
                            deliverableDao.update(deliverable);
                            
                            Log.d(TAG, "✅ Livrable sync: id=" + uploadedDeliverable.getDeliverableId());
                        } else {
                            Log.e(TAG, "❌ Erreur upload livrable API: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Exception upload livrable: " + e.getMessage());
                    }
                }
            }
            
            if (uploadCount > 0) {
                Log.d(TAG, "📤 " + uploadCount + " livrable(s) synchronisé(s)");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur syncPendingDeliverables: " + e.getMessage());
        }
    }
    
    // ════════════════════════════════════════════════════════════
    // Helper: Mapper les états du backend vers Room
    // ════════════════════════════════════════════════════════════
    private ConventionState mapBackendState(String backendState) {
        if (backendState == null) return ConventionState.PENDING;
        
        switch (backendState) {
            case "DEMAND_PENDING": return ConventionState.PENDING;
            case "DEMAND_APPROVED": return ConventionState.GENERATED;
            case "DEMAND_REJECTED": return ConventionState.REFUSED;
            case "SIGNED_UPLOADED": return ConventionState.UPLOADED;  // ✅ Backend SIGNED_UPLOADED = Room UPLOADED
            case "UPLOAD_REJECTED": return ConventionState.REJECTED;
            case "VALIDATED": return ConventionState.VALIDATED;
            default:
                try {
                    return ConventionState.valueOf(backendState);
                } catch (Exception e) {
                    return ConventionState.PENDING;
                }
        }
    }
    
    // ════════════════════════════════════════════════════════════
    // Helper: Récupérer l'ID étudiant pour une convention
    // ════════════════════════════════════════════════════════════
    private Long getStudentIdForConvention(Long pfaId) {
        try {
            PFADossierDao pfaDossierDao = database.pfaDossierDao();
            PFADossier pfaDossier = pfaDossierDao.getById(pfaId);
            if (pfaDossier != null) {
                return pfaDossier.getStudent_id();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération du studentId", e);
        }
        return null;
    }
    
    // ════════════════════════════════════════════════════════════
    // ════════════════════════════════════════════════════════════
    // SYNC INITIAL (Load all user data from backend on login)
    // ════════════════════════════════════════════════════════════
    
    public void syncUserDataFromBackend(Long studentId) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Démarrage de la synchronisation des données utilisateur...");
                updateSyncStatus(SyncStatus.SYNCING, "Récupération des dossiers PFA...");
                
                // 1. Récupérer les PFA dossiers de l'étudiant
                Call<List<PFADossierResponse>> pfaCall = apiService.getPFADossiersByStudent(studentId);
                Response<List<PFADossierResponse>> pfaResponse = pfaCall.execute();
                
                if (!pfaResponse.isSuccessful() || pfaResponse.body() == null) {
                    Log.e(TAG, "❌ Erreur lors de la récupération des PFA dossiers");
                    updateSyncStatus(SyncStatus.ERROR, "Erreur: impossible de récupérer les données");
                    return;
                }
                
                List<PFADossierResponse> pfaDossierResponses = pfaResponse.body();
                if (pfaDossierResponses == null || pfaDossierResponses.isEmpty()) {
                    Log.d(TAG, "✅ Aucun PFA dossier trouvé pour cet étudiant");
                    updateSyncStatus(SyncStatus.SUCCESS, "Pas de dossier PFA");
                    return;
                }
                
                PFADossierDao pfaDossierDao = database.pfaDossierDao();
                
                // 2. Pour chaque PFA dossier, récupérer et insérer les données associées
                for (PFADossierResponse pfaResponse2 : pfaDossierResponses) {
                    Long pfaId = pfaResponse2.getPfaId();
                    
                    // Insérer ou mettre à jour le PFA dossier
                    PFADossier pfaDossier = convertPFADossierResponseToEntity(pfaResponse2);
                    // ✅ Utiliser le backend_id comme local pfa_id
                    pfaDossier.setPfa_id(pfaId);
                    pfaDossier.setBackend_pfa_id(pfaId);
                    pfaDossier.setIs_synced(true);
                    pfaDossierDao.insert(pfaDossier);
                    Log.d(TAG, "✅ PFA dossier inséré: " + pfaId);
                    
                    // 2a. Récupérer la convention
                    updateSyncStatus(SyncStatus.SYNCING, "Récupération des conventions...");
                    syncConventionForPFA(pfaId);
                    
                    // 2b. Récupérer les livrables
                    updateSyncStatus(SyncStatus.SYNCING, "Récupération des livrables...");
                    syncDeliverablesForPFA(pfaId);
                    
                    // 2c. Récupérer les soutenances
                    updateSyncStatus(SyncStatus.SYNCING, "Récupération des soutenances...");
                    syncSoutenanceForPFA(pfaId);
                    
                    // 2d. Récupérer les évaluations
                    updateSyncStatus(SyncStatus.SYNCING, "Récupération des évaluations...");
                    syncEvaluationsForPFA(pfaId);
                }
                
                Log.d(TAG, "✅ Synchronisation complète des données utilisateur réussie!");
                updateSyncStatus(SyncStatus.SUCCESS, "Données synchronisées avec succès");
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur lors de la synchronisation: " + e.getMessage(), e);
                updateSyncStatus(SyncStatus.ERROR, "Erreur: " + e.getMessage());
            }
        });
    }
    
    private void syncConventionForPFA(Long pfaId) {
        try {
            Call<ConventionResponse> conventionCall = apiService.getConventionByPfaId(pfaId);
            Response<ConventionResponse> response = conventionCall.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                ConventionResponse conventionResponse = response.body();
                if (conventionResponse != null) {
                    Convention convention = convertConventionResponseToEntity(conventionResponse);
                    // ✅ Utiliser le backend_id comme local convention_id
                    convention.setConvention_id(conventionResponse.getConventionId());
                    convention.setBackend_convention_id(conventionResponse.getConventionId());
                    convention.setIs_synced(true);
                    convention.setPfa_id(pfaId);
                    
                    ConventionDao conventionDao = database.conventionDao();
                    conventionDao.insert(convention);
                    Log.d(TAG, "✅ Convention insérée pour PFA: " + pfaId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Erreur lors de la récupération de la convention pour PFA " + pfaId + ": " + e.getMessage());
        }
    }
    
    private void syncDeliverablesForPFA(Long pfaId) {
        try {
            Call<List<DeliverableResponse>> deliverablesCall = apiService.getDeliverablesByPfaId(pfaId);
            Response<List<DeliverableResponse>> response = deliverablesCall.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                List<DeliverableResponse> deliverables = response.body();
                if (deliverables != null && !deliverables.isEmpty()) {
                    DeliverableDao deliverableDao = database.deliverableDao();
                    
                    for (DeliverableResponse deliverableResponse : deliverables) {
                        Deliverable deliverable = convertDeliverableResponseToEntity(deliverableResponse);
                        // ✅ Utiliser le backend_id comme local deliverable_id
                        deliverable.setDeliverable_id(deliverableResponse.getDeliverableId());
                        deliverable.setBackend_deliverable_id(deliverableResponse.getDeliverableId());
                        deliverable.setIs_synced(true);
                        deliverable.setPfa_id(pfaId);
                        
                        deliverableDao.insert(deliverable);
                    }
                    Log.d(TAG, "✅ " + deliverables.size() + " livrables insérés pour PFA: " + pfaId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Erreur lors de la récupération des livrables pour PFA " + pfaId + ": " + e.getMessage());
        }
    }
    
    private void syncSoutenanceForPFA(Long pfaId) {
        try {
            Call<ApiResponse<SoutenanceResponse>> soutenanceCall = apiService.getSoutenanceByPfaId(pfaId);
            Response<ApiResponse<SoutenanceResponse>> response = soutenanceCall.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                // À implémenter selon ta structure de données
                Log.d(TAG, "✅ Soutenance récupérée pour PFA: " + pfaId);
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Erreur lors de la récupération de la soutenance pour PFA " + pfaId + ": " + e.getMessage());
        }
    }
    
    private void syncEvaluationsForPFA(Long pfaId) {
        try {
            Call<List<EvaluationResponse>> evaluationsCall = apiService.getEvaluationsByPfaId(pfaId);
            Response<List<EvaluationResponse>> response = evaluationsCall.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                // À implémenter selon ta structure de données
                Log.d(TAG, "✅ Évaluations récupérées pour PFA: " + pfaId);
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Erreur lors de la récupération des évaluations pour PFA " + pfaId + ": " + e.getMessage());
        }
    }
    
    // Convertir les réponses API en entités Room
    private PFADossier convertPFADossierResponseToEntity(PFADossierResponse response) {
        PFADossier entity = new PFADossier();
        entity.setStudent_id(response.getStudentId());
        entity.setSupervisor_id(response.getSupervisorId());
        entity.setTitle(response.getTitle());
        entity.setDescription(response.getDescription());
        entity.setCurrent_status(PFAStatus.valueOf(response.getCurrentStatus()));
        entity.setUpdated_at(response.getUpdatedAt());
        return entity;
    }
    
    private Convention convertConventionResponseToEntity(ConventionResponse response) {
        Convention entity = new Convention();
        entity.setCompany_name(response.getCompanyName());
        entity.setCompany_address(response.getCompanyAddress());
        entity.setCompany_supervisor_name(response.getCompanySupervisorName());
        entity.setCompany_supervisor_email(response.getCompanySupervisorEmail());
        entity.setStart_date(response.getStartDate());
        entity.setEnd_date(response.getEndDate());
        entity.setScanned_file_uri(response.getScannedFileUri());
        entity.setIs_validated(response.getIsValidated());
        entity.setState(ConventionState.UPLOADED);
        entity.setAdmin_comment(response.getAdminComment());
        return entity;
    }
    
    private Deliverable convertDeliverableResponseToEntity(DeliverableResponse response) {
        Deliverable entity = new Deliverable();
        entity.setFile_title(response.getFileTitle());
        entity.setFile_uri(response.getFileUri());
        entity.setUploaded_at(response.getUploadedAt());
        
        // Debug logging
        String rawFileType = response.getDeliverableFileType();
        Log.d(TAG, "📋 Deliverable: title=" + response.getFileTitle() + 
            ", rawFileType=" + rawFileType + 
            ", rawType=" + response.getDeliverableType());
        
        DeliverableFileType mappedFileType = mapDeliverableFileType(rawFileType);
        entity.setDeliverable_file_type(mappedFileType);
        Log.d(TAG, "   → mappedFileType=" + mappedFileType);
        
        entity.setDeliverable_type(mapDeliverableType(response.getDeliverableType()));
        return entity;
    }
    
    private DeliverableFileType mapDeliverableFileType(String fileType) {
        Log.d(TAG, "🔄 mapDeliverableFileType: input='" + fileType + "'");
        if (fileType == null) {
            Log.w(TAG, "⚠️ fileType is NULL!");
            return null;
        }
        
        switch (fileType.trim().toUpperCase()) {
            case "PROGRESS_REPORT": 
                Log.d(TAG, "✓ Mapped PROGRESS_REPORT → RAPPORT_AVANCEMENT");
                return DeliverableFileType.RAPPORT_AVANCEMENT;
            case "PRESENTATION": 
                Log.d(TAG, "✓ Mapped PRESENTATION → PRESENTATION");
                return DeliverableFileType.PRESENTATION;
            case "FINAL_REPORT": 
                Log.d(TAG, "✓ Mapped FINAL_REPORT → RAPPORT_FINAL");
                return DeliverableFileType.RAPPORT_FINAL;
            default:
                Log.w(TAG, "⚠️ Unknown fileType: '" + fileType + "', trying valueOf...");
                try {
                    return DeliverableFileType.valueOf(fileType);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Failed to map fileType: " + fileType + ", error: " + e.getMessage());
                    return null;
                }
        }
    }
    
    private DeliverableType mapDeliverableType(String type) {
        if (type == null) return DeliverableType.BEFORE_DEFENSE;
        try {
            return DeliverableType.valueOf(type);
        } catch (Exception e) {
            return DeliverableType.BEFORE_DEFENSE;
        }
    }
    
    private void updateSyncStatus(SyncStatus status, String message) {
        syncStatus.postValue(status);
        syncMessage.postValue(message);
    }

    // Getters pour les LiveData de synchronisation
    // ════════════════════════════════════════════════════════════
    public LiveData<SyncStatus> getSyncStatus() {
        return syncStatus;
    }
    
    public LiveData<String> getSyncMessage() {
        return syncMessage;
    }
    
    public boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
    
    // ════════════════════════════════════════════════════════════
    // Synchroniser les données en attente (Conventions)
    // ════════════════════════════════════════════════════════════
    public void syncPendingData() {
        executorService.execute(() -> {
            try {
                isSyncing = true;  // Marquer qu'on est en cours de sync
                syncStatus.postValue(SyncStatus.SYNCING);
                syncMessage.postValue("Synchronisation en cours...");
                
                Log.d(TAG, "🔄 Début de la synchronisation des données en attente");
                
                // Récupérer les conventions non synchronisées
                ConventionDao conventionDao = database.conventionDao();
                List<Convention> pendingConventions = conventionDao.getByState(ConventionState.PENDING);
                
                int successCount = 0;
                int errorCount = 0;
                
                // Synchroniser chaque convention en attente
                if (pendingConventions != null && !pendingConventions.isEmpty()) {
                    Log.d(TAG, "📤 " + pendingConventions.size() + " convention(s) à synchroniser");
                    
                    for (Convention convention : pendingConventions) {
                        try {
                            if (syncConvention(convention)) {
                                successCount++;
                            } else {
                                errorCount++;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Erreur lors de la sync d'une convention", e);
                            errorCount++;
                        }
                    }
                }
                
                // Synchroniser les uploads de conventions signées
                syncPendingUploads(conventionDao);
                
                // Synchroniser les livrables en attente
                syncPendingDeliverables();
                
                // ✅ NOUVELLE LOGIQUE: Synchronisation bidirectionnelle avec backend
                // (garder la logique de espace-administrateur)
                syncAll(application);
                
                // Mettre à jour le statut final
                if (errorCount == 0) {
                    syncStatus.postValue(SyncStatus.SUCCESS);
                    syncMessage.postValue("✅ Synchronisation réussie");
                    Log.d(TAG, "✅ Synchronisation réussie: " + successCount + " convention(s)");
                } else {
                    syncStatus.postValue(SyncStatus.ERROR);
                    syncMessage.postValue("⚠️ " + successCount + " réussi(es), " + errorCount + " erreur(s)");
                    Log.w(TAG, "⚠️ Synchronisation partielle: " + successCount + " réussi(es), " + errorCount + " erreur(s)");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur générale lors de la synchronisation", e);
                syncStatus.postValue(SyncStatus.ERROR);
                syncMessage.postValue("Erreur de synchronisation");
            } finally {
                isSyncing = false;  // Toujours marquer fin de sync
            }
        });
    }
    
    private static void log(String msg) {
        Log.d("SyncManager", msg);
    }

    // ════════════════════════════════════════════════════════════
    // Upload local changes to backend for all entities
    // ════════════════════════════════════════════════════════════
    public static void uploadAll(Context context) {
        log("--- uploadAll START ---");
        uploadDepartments(context);
        uploadUsers(context);
        uploadPFADossiers(context);
        uploadConventions(context);
        log("--- uploadAll END ---");
    }

    public static void uploadDepartments(Context context) {
        log("uploadDepartments: called");
        DepartmentApi api = retrofit.create(DepartmentApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        List<Department> departments = db.departmentDao().getAll();
        log("uploadDepartments: count=" + departments.size());
        for (Department dept : departments) {
            try {
                if (dept.getDepartment_id() == null || dept.getDepartment_id() == 0) {
                    log("uploadDepartments: createDepartment " + dept.getName());
                    api.createDepartment(dept).execute();
                } else {
                    log("uploadDepartments: updateDepartment id=" + dept.getDepartment_id());
                    api.updateDepartment(dept.getDepartment_id(), dept).execute();
                }
            } catch (Exception e) {
                Log.e("SyncManager", "Department upload failed", e);
            }
        }
    }

    public static void uploadUsers(Context context) {
        log("uploadUsers: called");
        UserApi api = retrofit.create(UserApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        List<User> users = db.userDao().getAllUsers();
        log("uploadUsers: count=" + users.size());
        for (User user : users) {
            try {
                if (user.getUser_id() == null || user.getUser_id() == 0) {
                    log("uploadUsers: createUser " + user.getEmail());
                    api.createUser(user).execute();
                } else {
                    log("uploadUsers: updateUser id=" + user.getUser_id());
                    api.updateUser(user.getUser_id(), user).execute();
                }
            } catch (Exception e) {
                Log.e("SyncManager", "User upload failed", e);
            }
        }
    }

    public static void uploadPFADossiers(Context context) {
        log("uploadPFADossiers: called");
        PFADossierApi api = retrofit.create(PFADossierApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        List<PFADossier> dossiers = db.pfaDossierDao().getAll();
        log("uploadPFADossiers: count=" + dossiers.size());
        for (PFADossier dossier : dossiers) {
            try {
                if (dossier.getPfa_id() == null || dossier.getPfa_id() == 0) {
                    log("uploadPFADossiers: createPFADossier " + dossier.getTitle());
                    api.createPFADossier(dossier).execute();
                } else {
                    log("uploadPFADossiers: updatePFADossier id=" + dossier.getPfa_id());
                    api.updatePFADossier(dossier.getPfa_id(), dossier).execute();
                }
            } catch (Exception e) {
                Log.e("SyncManager", "PFADossier upload failed", e);
            }
        }
    }

    public static void uploadConventions(Context context) {
        log("uploadConventions: called");
        ConventionApi api = retrofit.create(ConventionApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        List<Convention> conventions = db.conventionDao().getAll();
        log("uploadConventions: count=" + conventions.size());
        for (Convention conv : conventions) {
            try {
                if (conv.getConvention_id() == null || conv.getConvention_id() == 0) {
                    log("uploadConventions: createConvention");
                    api.createConvention(conv).execute();
                } else {
                    log("uploadConventions: updateConvention id=" + conv.getConvention_id());
                    api.updateConvention(conv.getConvention_id(), conv).execute();
                }
            } catch (Exception e) {
                Log.e("SyncManager", "Convention upload failed", e);
            }
        }
    }

    private static final String BASE_URL = "http://10.0.2.2:8080"; // Adapter selon ton backend
    private static final com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
        .setLenient()
        .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.IDENTITY)
        .create();
    private static Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();

    public static void syncAll(Context context) {
        // Synchroniser d'abord les parents, puis les enfants pour respecter les clés étrangères
        syncDepartments(context);   // parents
        syncUsers(context);         // dépend de departments
        syncPFADossiers(context);   // dépend de users
        syncConventions(context);   // dépend de pfa_dossiers/users
        log("--- syncAll END ---");
    }

    public static void syncDepartments(Context context) {
        DepartmentApi api = retrofit.create(DepartmentApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<Department>> response = api.getAllDepartments().execute();
            if (response.isSuccessful()) {
                String rawJson = response.errorBody() != null ? response.errorBody().string() : null;
                if (rawJson != null) Log.d("SyncManager", "Department raw JSON: " + rawJson);
                if (response.body() != null) {
                    List<Department> remoteDepartments = response.body();
                    Log.d("SyncManager", "Departments reçus: " + remoteDepartments.size());
                    for (Department dep : remoteDepartments) {
                        Log.d("SyncManager", "Department reçu: id=" + dep.getDepartment_id() + ", name=" + dep.getName());
                        if (dep.getDepartment_id() == null) continue;
                        Department local = db.departmentDao().getById(dep.getDepartment_id());
                        if (local != null) {
                            db.departmentDao().update(dep);
                        } else {
                            db.departmentDao().insert(dep);
                        }
                    }
                    // Ne pas supprimer les départements absents de la réponse pour éviter les cascades qui vident les users.
                } else {
                    Log.d("SyncManager", "Department: body null");
                }
            } else {
                Log.d("SyncManager", "Department: response not successful");
            }
        } catch (Exception e) {
            Log.e("SyncManager", "Department sync failed", e);
        }
    }

    public static void syncUsers(Context context) {
        UserApi api = retrofit.create(UserApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<User>> response = api.getAllUsers().execute();
            if (response.isSuccessful() && response.body() != null) {
                List<User> remoteUsers = response.body();
                Log.d("SyncManager", "Users reçus: " + remoteUsers.size());
                for (User user : remoteUsers) {
                    Log.d("SyncManager", "User reçu: id=" + user.getUser_id() + ", email=" + user.getEmail() + ", firstName=" + user.getFirst_name());
                    if (user.getUser_id() == null) continue;
                    User local = db.userDao().getUserByIdSync(user.getUser_id());
                    if (local != null) {
                        // Merger: ne pas écraser avec null
                        if (user.getFirst_name() != null) local.setFirst_name(user.getFirst_name());
                        if (user.getLast_name() != null) local.setLast_name(user.getLast_name());
                        if (user.getEmail() != null) local.setEmail(user.getEmail());
                        if (user.getPassword() != null) local.setPassword(user.getPassword());
                        if (user.getRole() != null) local.setRole(user.getRole());
                        if (user.getPhone_number() != null) local.setPhone_number(user.getPhone_number());
                        if (user.getCreated_at() != null) local.setCreated_at(user.getCreated_at());
                        if (user.getDepartment_id() != null) local.setDepartment_id(user.getDepartment_id());
                        db.userDao().update(local);
                    } else {
                        db.userDao().insert(user);
                    }
                }
                // Do NOT delete local users that are not returned yet by backend (e.g., freshly created offline).
                // Previously we removed any user whose id was absent from the remote list, which wiped Room
                // after inserting a new student locally then syncing. We keep locals to avoid losing accounts.
            } else {
                Log.d("SyncManager", "User: rien reçu");
            }
        } catch (Exception e) {
            Log.e("SyncManager", "User sync failed", e);
        }
    }

    public static void syncPFADossiers(Context context) {
        PFADossierApi api = retrofit.create(PFADossierApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<PFADossier>> response = api.getAllPFADossiers().execute();
            if (response.isSuccessful() && response.body() != null) {
                List<PFADossier> remoteDossiers = response.body();
                Log.d("SyncManager", "PFADossiers reçus: " + remoteDossiers.size());
                for (PFADossier dossier : remoteDossiers) {
                    Log.d("SyncManager", "PFADossier reçu: id=" + dossier.getPfa_id() + ", title=" + dossier.getTitle() + ", status=" + dossier.getCurrent_status());
                    if (dossier.getPfa_id() == null) continue;
                    PFADossier local = db.pfaDossierDao().getById(dossier.getPfa_id());
                    if (local != null) {
                        Log.d("SyncManager", "PFADossier local avant update: id=" + local.getPfa_id() + ", status=" + local.getCurrent_status());
                        // Merger: ne pas écraser avec null
                        if (dossier.getStudent_id() != null) local.setStudent_id(dossier.getStudent_id());
                        if (dossier.getSupervisor_id() != null) local.setSupervisor_id(dossier.getSupervisor_id());
                        if (dossier.getTitle() != null) local.setTitle(dossier.getTitle());
                        if (dossier.getDescription() != null) local.setDescription(dossier.getDescription());
                        if (dossier.getCurrent_status() != null) local.setCurrent_status(dossier.getCurrent_status());
                        if (dossier.getUpdated_at() != null) local.setUpdated_at(dossier.getUpdated_at());
                        db.pfaDossierDao().update(local);
                        PFADossier updated = db.pfaDossierDao().getById(dossier.getPfa_id());
                        Log.d("SyncManager", "PFADossier local après update: id=" + updated.getPfa_id() + ", status=" + updated.getCurrent_status());
                    } else {
                        db.pfaDossierDao().insert(dossier);
                    }
                }
                List<Long> remoteIds = new java.util.ArrayList<>();
                for (PFADossier dossier : remoteDossiers) {
                    if (dossier.getPfa_id() != null) remoteIds.add(dossier.getPfa_id());
                }
                Log.d("SyncManager", "PFADossier IDs conservés: " + remoteIds);
                db.pfaDossierDao().deleteNotInIds(remoteIds);
            } else {
                Log.d("SyncManager", "PFADossier: rien reçu - code: " + response.code() + ", message: " + response.message());
            }
        } catch (Exception e) {
            Log.e("SyncManager", "❌ PFADossier sync failed: " + e.getMessage(), e);
        }
    }

    public static void syncConventions(Context context) {
        ConventionApi api = retrofit.create(ConventionApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<Convention>> response = api.getAllConventions().execute();
            if (response.isSuccessful() && response.body() != null) {
                List<Convention> remoteConventions = response.body();
                Log.d("SyncManager", "Conventions reçues: " + remoteConventions.size());
                for (Convention conv : remoteConventions) {
                    Log.d("SyncManager", "Convention reçue: id=" + conv.getConvention_id() + ", company=" + conv.getCompany_name());
                    if (conv.getConvention_id() == null) continue;
                    Convention local = db.conventionDao().getById(conv.getConvention_id());
                    if (local != null) {
                        // Merger: ne pas écraser avec null
                        if (conv.getPfa_id() != null) local.setPfa_id(conv.getPfa_id());
                        if (conv.getCompany_name() != null) local.setCompany_name(conv.getCompany_name());
                        if (conv.getCompany_address() != null) local.setCompany_address(conv.getCompany_address());
                        if (conv.getCompany_supervisor_name() != null) local.setCompany_supervisor_name(conv.getCompany_supervisor_name());
                        if (conv.getCompany_supervisor_email() != null) local.setCompany_supervisor_email(conv.getCompany_supervisor_email());
                        if (conv.getStart_date() != null) local.setStart_date(conv.getStart_date());
                        if (conv.getEnd_date() != null) local.setEnd_date(conv.getEnd_date());
                        if (conv.getState() != null) local.setState(conv.getState());
                        if (conv.getScanned_file_uri() != null) local.setScanned_file_uri(conv.getScanned_file_uri());
                        if (conv.getIs_validated() != null) local.setIs_validated(conv.getIs_validated());
                        if (conv.getAdmin_comment() != null) local.setAdmin_comment(conv.getAdmin_comment());
                        db.conventionDao().update(local);
                    } else {
                        db.conventionDao().insert(conv);
                    }
                }
                List<Long> remoteIds = new java.util.ArrayList<>();
                for (Convention conv : remoteConventions) {
                    if (conv.getConvention_id() != null) remoteIds.add(conv.getConvention_id());
                }
                Log.d("SyncManager", "Convention IDs conservés: " + remoteIds);
                db.conventionDao().deleteNotInIds(remoteIds);
            } else {
                Log.d("SyncManager", "⚠️ Convention: rien reçu - code: " + response.code() + ", message: " + response.message());
            }
        } catch (Exception e) {
            Log.e("SyncManager", "❌ Convention sync failed: " + e.getMessage(), e);
        }
    }
}

