package ma.ensate.pfa_manager.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.ConventionDao;
import ma.ensate.pfa_manager.database.DeliverableDao;
import ma.ensate.pfa_manager.database.EvaluationDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.SoutenanceDao;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.*;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.ConventionResponse;
import ma.ensate.pfa_manager.model.api.DeliverableResponse;
import ma.ensate.pfa_manager.model.api.SoutenanceResponse;
import ma.ensate.pfa_manager.model.api.StudentDetailResponse;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDetailRepository {

    private static final String TAG = "StudentDetailRepository";

    private final UserDao userDao;
    private final PFADossierDao pfaDao;
    private final DeliverableDao deliverableDao;
    private final SoutenanceDao soutenanceDao;
    private final ConventionDao conventionDao;
    private final EvaluationDao evaluationDao;
    private final ApiService apiService;
    private final ExecutorService executor;

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public StudentDetailRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        pfaDao = db.pfaDossierDao();
        deliverableDao = db.deliverableDao();
        soutenanceDao = db.soutenanceDao();
        conventionDao = db.conventionDao();
        evaluationDao = db.evaluationDao();
        apiService = ApiClient.getApiService();
        executor = Executors.newSingleThreadExecutor();
    }


    public LiveData<User> getStudent(Long studentId) {
        return userDao.getUserById(studentId);
    }

    public LiveData<List<PFADossier>> getStudentPFAs(Long studentId) {
        return pfaDao.getPFAsByStudent(studentId);
    }

    public LiveData<List<Deliverable>> getPFADeliverables(Long pfaId) {
        return deliverableDao.getByPfaId(pfaId);
    }

    public LiveData<Soutenance> getPFASoutenance(Long pfaId) {
        return soutenanceDao.getSoutenanceByPFA(pfaId);
    }

    public LiveData<Convention> getPFAConvention(Long pfaId) {
        return conventionDao.getConventionByPFA(pfaId);
    }

    public LiveData<Integer> countDeliverables(Long pfaId) {
        return deliverableDao.getCountByPfaId(pfaId);
    }

    public LiveData<Evaluation> getPFAEvaluation(Long pfaId) {
        return evaluationDao.getByPfaIdLive(pfaId);
    }

    public void syncStudentDetail(Long supervisorId, Long studentId) {
        isSyncing.postValue(true);
        Log.d(TAG, "🔄 Sync détail étudiant: " + studentId);

        apiService.getStudentDetail(studentId, supervisorId).enqueue(new Callback<ApiResponse<StudentDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StudentDetailResponse>> call,
                                   Response<ApiResponse<StudentDetailResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<StudentDetailResponse> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        saveStudentDetailToRoom(apiResponse.getData());
                        Log.d(TAG, "✅ Sync détail réussie");
                    } else {
                        Log.e(TAG, "❌ API error: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ HTTP error: " + response.code());
                }
                isSyncing.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<StudentDetailResponse>> call, Throwable t) {
                Log.e(TAG, "❌ Network error: " + t.getMessage());
                isSyncing.postValue(false);
            }
        });
    }


    private void saveStudentDetailToRoom(StudentDetailResponse data) {
        executor.execute(() -> {
            try {
                Long pfaId = data.getPfaId();

                // ═══════════════════════════════════════════════════════
                // 1. Convention
                // ═══════════════════════════════════════════════════════
                if (data.getConvention() != null) {
                    saveConvention(data.getConvention());
                }

                // ═══════════════════════════════════════════════════════
                // 2. Livrables
                // ═══════════════════════════════════════════════════════
                if (data.getDeliverables() != null) {
                    for (DeliverableResponse dr : data.getDeliverables()) {
                        saveDeliverable(dr);
                    }
                    Log.d(TAG, "✅ " + data.getDeliverables().size() + " livrables sauvegardés");
                }

                // ═══════════════════════════════════════════════════════
                // 3. Soutenance
                // ═══════════════════════════════════════════════════════
                if (data.getSoutenance() != null) {
                    saveSoutenance(data.getSoutenance());
                }

                // ═══════════════════════════════════════════════════════
                // 4. Évaluation
                // ═══════════════════════════════════════════════════════
                if (data.getIsEvaluated() != null && data.getIsEvaluated() && data.getTotalScore() != null) {
                    saveEvaluation(pfaId, data.getTotalScore());
                }

                Log.d(TAG, "✅ Toutes les données sauvegardées pour étudiant " + data.getStudentId());

            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur sauvegarde: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTHODES DE SAUVEGARDE INDIVIDUELLES
    // ═══════════════════════════════════════════════════════════════════════

    private void saveConvention(ConventionResponse cr) {
        Convention existing = conventionDao.getByPfaId(cr.getPfaId());
        boolean isNew = (existing == null);

        Convention convention = isNew ? new Convention() : existing;

        convention.setConvention_id(cr.getConventionId());
        convention.setPfa_id(cr.getPfaId());
        convention.setCompany_name(cr.getCompanyName());
        convention.setCompany_address(cr.getCompanyAddress());
        convention.setCompany_supervisor_name(cr.getCompanySupervisorName());
        convention.setCompany_supervisor_email(cr.getCompanySupervisorEmail());
        convention.setStart_date(cr.getStartDate());
        convention.setEnd_date(cr.getEndDate());
        convention.setScanned_file_uri(cr.getScannedFileUri());
        convention.setIs_validated(cr.getIsValidated());
        convention.setState(mapConventionState(cr.getState()));
        convention.setAdmin_comment(cr.getAdminComment());

        if (isNew) {
            conventionDao.insert(convention);
            Log.d(TAG, "✅ INSERT Convention ID: " + convention.getConvention_id());
        } else {
            conventionDao.update(convention);
            Log.d(TAG, "✅ UPDATE Convention ID: " + convention.getConvention_id());
        }
    }

    private void saveDeliverable(DeliverableResponse dr) {
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
            Log.d(TAG, "✅ INSERT Deliverable: " + deliverable.getFile_title());
        } else {
            deliverableDao.update(deliverable);
            Log.d(TAG, "✅ UPDATE Deliverable: " + deliverable.getFile_title());
        }
    }

    private void saveSoutenance(SoutenanceResponse sr) {
        Soutenance existing = soutenanceDao.getByPfaIdSync(sr.getPfaId());
        boolean isNew = (existing == null);

        Soutenance soutenance = isNew ? new Soutenance() : existing;

        soutenance.setSoutenance_id(sr.getSoutenanceId());
        soutenance.setPfa_id(sr.getPfaId());
        soutenance.setLocation(sr.getLocation());
        soutenance.setDate_soutenance(sr.getDateSoutenance());
        soutenance.setStatus(mapSoutenanceStatus(sr.getStatus()));
        soutenance.setCreated_at(sr.getCreatedAt());

        if (isNew) {
            soutenanceDao.insert(soutenance);
            Log.d(TAG, "✅ INSERT Soutenance ID: " + soutenance.getSoutenance_id());
        } else {
            soutenanceDao.update(soutenance);
            Log.d(TAG, "✅ UPDATE Soutenance ID: " + soutenance.getSoutenance_id());
        }
    }

    private void saveEvaluation(Long pfaId, Double totalScore) {
        Evaluation existing = evaluationDao.getByPfaIdSync(pfaId);
        boolean isNew = (existing == null);

        Evaluation evaluation = isNew ? new Evaluation() : existing;

        evaluation.setPfa_id(pfaId);
        evaluation.setTotal_score(totalScore);

        if (isNew) {
            evaluationDao.insert(evaluation);
            Log.d(TAG, "✅ INSERT Evaluation");
        } else {
            evaluationDao.update(evaluation);
            Log.d(TAG, "✅ UPDATE Evaluation");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MAPPERS (API → Room Enums)
    // ═══════════════════════════════════════════════════════════════════════

    private ConventionState mapConventionState(String state) {
        if (state == null) return ConventionState.PENDING;

        // Mapping backend → Android
        switch (state) {
            case "DEMAND_PENDING": return ConventionState.PENDING;
            case "DEMAND_APPROVED": return ConventionState.GENERATED;
            case "DEMAND_REJECTED": return ConventionState.REFUSED;
            case "SIGNED_UPLOADED": return ConventionState.UPLOADED;
            case "UPLOAD_REJECTED": return ConventionState.REJECTED;
            case "VALIDATED": return ConventionState.VALIDATED;
            default:
                try {
                    return ConventionState.valueOf(state);
                } catch (Exception e) {
                    return ConventionState.PENDING;
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

    private DeliverableFileType mapDeliverableFileType(String fileType) {
        if (fileType == null) return null;

        // Mapping backend → Android
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

    private SoutenanceStatus mapSoutenanceStatus(String status) {
        if (status == null) return SoutenanceStatus.PLANNED;
        try {
            return SoutenanceStatus.valueOf(status);
        } catch (Exception e) {
            return SoutenanceStatus.PLANNED;
        }
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }
}