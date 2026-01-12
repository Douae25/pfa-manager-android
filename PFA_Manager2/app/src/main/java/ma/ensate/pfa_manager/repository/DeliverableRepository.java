package ma.ensate.pfa_manager.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.DeliverableDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.DeliverableFileType;
import ma.ensate.pfa_manager.model.DeliverableType;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.DeliverableResponse;
import ma.ensate.pfa_manager.model.dto.DeliverableWithStudent;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliverableRepository {

    private static final String TAG = "DeliverableRepository";

    private final DeliverableDao deliverableDao;
    private final PFADossierDao pfaDao;
    private final UserDao userDao;
    private final ApiService apiService;
    private final ExecutorService executor;

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public DeliverableRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        deliverableDao = db.deliverableDao();
        pfaDao = db.pfaDossierDao();
        userDao = db.userDao();
        apiService = ApiClient.getApiService();
        executor = Executors.newSingleThreadExecutor();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTHODES CRUD
    // ═══════════════════════════════════════════════════════════════════════

    public void insert(Deliverable deliverable) {
        executor.execute(() -> deliverableDao.insert(deliverable));
    }

    public void update(Deliverable deliverable) {
        executor.execute(() -> deliverableDao.update(deliverable));
    }

    public void delete(Deliverable deliverable) {
        executor.execute(() -> deliverableDao.delete(deliverable));
    }

    public LiveData<Deliverable> getById(Long id) {
        return deliverableDao.getById(id);
    }

    public LiveData<List<Deliverable>> getByPfaId(Long pfaId) {
        return deliverableDao.getByPfaId(pfaId);
    }

    public LiveData<List<Deliverable>> getBySupervisorId(Long supervisorId) {
        return deliverableDao.getBySupervisorId(supervisorId);
    }

    public LiveData<Integer> getCountBySupervisor(Long supervisorId) {
        return deliverableDao.getCountBySupervisor(supervisorId);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTHODE PRINCIPALE AVEC SYNC
    // ═══════════════════════════════════════════════════════════════════════

    public LiveData<List<DeliverableWithStudent>> getDeliverablesWithStudents(Long supervisorId) {
        // 1. Sync depuis l'API
        syncDeliverablesFromApi(supervisorId);

        // 2. Retourner le LiveData combiné de Room
        MediatorLiveData<List<DeliverableWithStudent>> result = new MediatorLiveData<>();

        LiveData<List<Deliverable>> deliverablesLiveData = deliverableDao.getBySupervisorId(supervisorId);
        LiveData<List<PFADossier>> pfasLiveData = pfaDao.getPFAsBySupervisor(supervisorId);
        LiveData<List<User>> studentsLiveData = userDao.getStudentsBySupervisor(supervisorId);

        result.addSource(deliverablesLiveData, deliverables -> {
            combineData(result, deliverables, pfasLiveData.getValue(), studentsLiveData.getValue());
        });

        result.addSource(pfasLiveData, pfas -> {
            combineData(result, deliverablesLiveData.getValue(), pfas, studentsLiveData.getValue());
        });

        result.addSource(studentsLiveData, students -> {
            combineData(result, deliverablesLiveData.getValue(), pfasLiveData.getValue(), students);
        });

        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SYNCHRONISATION DEPUIS L'API
    // ═══════════════════════════════════════════════════════════════════════

    public void syncDeliverablesFromApi(Long supervisorId) {
        isSyncing.postValue(true);
        Log.d(TAG, "🔄 Sync livrables pour superviseur: " + supervisorId);

        apiService.getAllDeliverables(supervisorId).enqueue(new Callback<ApiResponse<List<DeliverableResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DeliverableResponse>>> call,
                                   Response<ApiResponse<List<DeliverableResponse>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<DeliverableResponse>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        saveDeliverablesLocally(apiResponse.getData());
                        Log.d(TAG, "✅ Sync réussie: " + apiResponse.getData().size() + " livrables");
                    } else {
                        Log.e(TAG, "❌ API error: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ HTTP error: " + response.code());
                }
                isSyncing.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DeliverableResponse>>> call, Throwable t) {
                Log.e(TAG, "❌ Network error: " + t.getMessage());
                isSyncing.postValue(false);
            }
        });
    }

    private void saveDeliverablesLocally(List<DeliverableResponse> apiDeliverables) {
        executor.execute(() -> {
            for (DeliverableResponse dr : apiDeliverables) {
                try {
                    Deliverable existing = deliverableDao.getByIdSync(dr.getDeliverableId());
                    boolean isNew = (existing == null);

                    Deliverable deliverable = isNew ? new Deliverable() : existing;

                    deliverable.setDeliverable_id(dr.getDeliverableId());
                    deliverable.setPfa_id(dr.getPfaId());
                    deliverable.setFile_title(dr.getFileTitle());
                    deliverable.setFile_uri(dr.getFileUri());
                    deliverable.setDeliverable_type(mapDeliverableType(dr.getDeliverableType()));
                    deliverable.setDeliverable_file_type(mapDeliverableFileType(dr.getDeliverableFileType()));
                    deliverable.setUploaded_at(dr.getUploadedAt());

                    if (isNew) {
                        deliverableDao.insert(deliverable);
                        Log.d(TAG, "INSERT Deliverable: " + deliverable.getFile_title());
                    } else {
                        deliverableDao.update(deliverable);
                        Log.d(TAG, "UPDATE Deliverable: " + deliverable.getFile_title());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "❌ Erreur sauvegarde livrable: " + e.getMessage());
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════

    private DeliverableType mapDeliverableType(String type) {
        if (type == null) return DeliverableType.BEFORE_DEFENSE;
        try {
            return DeliverableType.valueOf(type);
        } catch (Exception e) {
            return DeliverableType.BEFORE_DEFENSE;
        }
    }

    private DeliverableFileType mapDeliverableFileType(String fileType) {
        if (fileType == null) return null;

        switch (fileType) {
            case "PROGRESS_REPORT": return DeliverableFileType.RAPPORT_AVANCEMENT;
            case "PRESENTATION": return DeliverableFileType.PRESENTATION;
            case "FINAL_REPORT": return DeliverableFileType.RAPPORT_FINAL;
            default:
                try {
                    return DeliverableFileType.valueOf(fileType);
                } catch (Exception e) {
                    return null;
                }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // COMBINE DATA
    // ═══════════════════════════════════════════════════════════════════════

    private void combineData(MediatorLiveData<List<DeliverableWithStudent>> result,
                             List<Deliverable> deliverables,
                             List<PFADossier> pfas,
                             List<User> students) {
        if (deliverables == null || pfas == null || students == null) {
            return;
        }

        Map<Long, PFADossier> pfaMap = new HashMap<>();
        for (PFADossier pfa : pfas) {
            pfaMap.put(pfa.getPfa_id(), pfa);
        }

        Map<Long, User> studentMap = new HashMap<>();
        for (User student : students) {
            studentMap.put(student.getUser_id(), student);
        }

        List<DeliverableWithStudent> combined = new ArrayList<>();
        for (Deliverable deliverable : deliverables) {
            PFADossier pfa = pfaMap.get(deliverable.getPfa_id());
            User student = null;
            if (pfa != null) {
                student = studentMap.get(pfa.getStudent_id());
            }
            combined.add(new DeliverableWithStudent(deliverable, pfa, student));
        }

        result.setValue(combined);
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public void forceRefresh(Long supervisorId) {
        syncDeliverablesFromApi(supervisorId);
    }
}