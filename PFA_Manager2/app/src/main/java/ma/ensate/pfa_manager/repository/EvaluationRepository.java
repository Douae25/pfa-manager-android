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

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.EvaluationCriteriaDao;
import ma.ensate.pfa_manager.database.EvaluationDao;
import ma.ensate.pfa_manager.database.EvaluationDetailDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.SoutenanceDao;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.*;
import ma.ensate.pfa_manager.model.api.*;
import ma.ensate.pfa_manager.model.dto.CriteriaWithScore;
import ma.ensate.pfa_manager.model.dto.SoutenanceWithEvaluation;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EvaluationRepository {

    private static final String TAG = "EvaluationRepository";

    private final EvaluationDao evaluationDao;
    private final EvaluationCriteriaDao criteriaDao;
    private final EvaluationDetailDao detailDao;
    private final SoutenanceDao soutenanceDao;
    private final PFADossierDao pfaDao;
    private final UserDao userDao;
    private final ApiService apiService;
    private final ExecutorService executor;

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public EvaluationRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        evaluationDao = db.evaluationDao();
        criteriaDao = db.evaluationCriteriaDao();
        detailDao = db.evaluationDetailDao();
        soutenanceDao = db.soutenanceDao();
        pfaDao = db.pfaDossierDao();
        userDao = db.userDao();
        apiService = ApiClient.getApiService();
        executor = Executors.newSingleThreadExecutor();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CRITÈRES D'ÉVALUATION (avec sync API)
    // ═══════════════════════════════════════════════════════════════════════

    public LiveData<List<EvaluationCriteria>> getActiveCriteria() {
        // Sync depuis l'API
        syncCriteriaFromApi();
        // Retourner le LiveData de Room
        return criteriaDao.getActiveLive();
    }

    private void syncCriteriaFromApi() {
        apiService.getEvaluationCriteria().enqueue(new Callback<ApiResponse<List<EvaluationCriteriaResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<EvaluationCriteriaResponse>>> call,
                                   Response<ApiResponse<List<EvaluationCriteriaResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveCriteriaLocally(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<EvaluationCriteriaResponse>>> call, Throwable t) {
                Log.w(TAG, "⚠️ Critères API failed, utilisation du cache local");
            }
        });
    }

    private void saveCriteriaLocally(List<EvaluationCriteriaResponse> apiCriteria) {
        executor.execute(() -> {
            for (EvaluationCriteriaResponse cr : apiCriteria) {
                try {
                    EvaluationCriteria existing = criteriaDao.getById(cr.getCriteriaId());
                    boolean isNew = (existing == null);

                    EvaluationCriteria criteria = isNew ? new EvaluationCriteria() : existing;

                    criteria.setCriteria_id(cr.getCriteriaId());
                    criteria.setLabel(cr.getLabel());
                    criteria.setWeight(cr.getWeight());
                    criteria.setDescription(cr.getDescription());
                    criteria.setIs_active(cr.getIsActive());

                    if (isNew) {
                        criteriaDao.insert(criteria);
                    } else {
                        criteriaDao.update(criteria);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erreur sauvegarde critère: " + e.getMessage());
                }
            }
            Log.d(TAG, "✅ Critères sync: " + apiCriteria.size());
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SOUTENANCES AVEC ÉVALUATIONS (avec sync API)
    // ═══════════════════════════════════════════════════════════════════════

    public LiveData<List<SoutenanceWithEvaluation>> getSoutenancesWithEvaluations(Long supervisorId) {
        // 1. Sync depuis l'API
        syncEvaluationsFromApi(supervisorId);

        // 2. Retourner le LiveData combiné de Room
        MediatorLiveData<List<SoutenanceWithEvaluation>> result = new MediatorLiveData<>();

        LiveData<List<Soutenance>> soutenancesLive = soutenanceDao.getAllSoutenancesBySupervisor(supervisorId);
        LiveData<List<PFADossier>> pfasLive = pfaDao.getPFAsBySupervisor(supervisorId);
        LiveData<List<User>> studentsLive = userDao.getStudentsBySupervisor(supervisorId);
        LiveData<List<Evaluation>> evaluationsLive = evaluationDao.getByEvaluatorLive(supervisorId);

        result.addSource(soutenancesLive, soutenances -> {
            combineData(result, soutenances, pfasLive.getValue(), studentsLive.getValue(), evaluationsLive.getValue());
        });

        result.addSource(pfasLive, pfas -> {
            combineData(result, soutenancesLive.getValue(), pfas, studentsLive.getValue(), evaluationsLive.getValue());
        });

        result.addSource(studentsLive, students -> {
            combineData(result, soutenancesLive.getValue(), pfasLive.getValue(), students, evaluationsLive.getValue());
        });

        result.addSource(evaluationsLive, evaluations -> {
            combineData(result, soutenancesLive.getValue(), pfasLive.getValue(), studentsLive.getValue(), evaluations);
        });

        return result;
    }

    private void syncEvaluationsFromApi(Long supervisorId) {
        isSyncing.postValue(true);

        apiService.getSoutenancesWithEvaluations(supervisorId).enqueue(new Callback<ApiResponse<List<SoutenanceWithEvaluationResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SoutenanceWithEvaluationResponse>>> call,
                                   Response<ApiResponse<List<SoutenanceWithEvaluationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveEvaluationsLocally(response.body().getData(), supervisorId);
                }
                isSyncing.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SoutenanceWithEvaluationResponse>>> call, Throwable t) {
                Log.w(TAG, "⚠️ Évaluations API failed, utilisation du cache local");
                isSyncing.postValue(false);
            }
        });
    }

    private void saveEvaluationsLocally(List<SoutenanceWithEvaluationResponse> apiData, Long supervisorId) {
        executor.execute(() -> {
            for (SoutenanceWithEvaluationResponse item : apiData) {
                try {
                    if (item.isEvaluated() && item.getTotalScore() != null) {
                        Evaluation existing = evaluationDao.getByPfaIdSync(item.getPfaId());
                        boolean isNew = (existing == null);

                        Evaluation evaluation = isNew ? new Evaluation() : existing;

                        evaluation.setPfa_id(item.getPfaId());
                        evaluation.setEvaluator_id(supervisorId);
                        evaluation.setTotal_score(item.getTotalScore());

                        if (isNew) {
                            evaluationDao.insert(evaluation);
                            Log.d(TAG, "✅ INSERT Evaluation PFA: " + item.getPfaId());
                        } else {
                            evaluationDao.update(evaluation);
                            Log.d(TAG, "✅ UPDATE Evaluation PFA: " + item.getPfaId());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erreur sauvegarde évaluation: " + e.getMessage());
                }
            }
        });
    }

    private void combineData(MediatorLiveData<List<SoutenanceWithEvaluation>> result,
                             List<Soutenance> soutenances,
                             List<PFADossier> pfas,
                             List<User> students,
                             List<Evaluation> evaluations) {
        if (soutenances == null || pfas == null || students == null) {
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

        Map<Long, Evaluation> evalMap = new HashMap<>();
        if (evaluations != null) {
            for (Evaluation eval : evaluations) {
                evalMap.put(eval.getPfa_id(), eval);
            }
        }

        List<SoutenanceWithEvaluation> combined = new ArrayList<>();
        for (Soutenance soutenance : soutenances) {
            PFADossier pfa = pfaMap.get(soutenance.getPfa_id());
            User student = null;
            if (pfa != null) {
                student = studentMap.get(pfa.getStudent_id());
            }
            Evaluation evaluation = evalMap.get(soutenance.getPfa_id());

            combined.add(new SoutenanceWithEvaluation(soutenance, pfa, student, evaluation));
        }

        result.setValue(combined);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SAUVEGARDER UNE ÉVALUATION (Local + Sync API)
    // ═══════════════════════════════════════════════════════════════════════

    public void saveEvaluation(Long pfaId, Long evaluatorId, List<CriteriaWithScore> criteriaScores,
                               OnEvaluationListener listener) {
        executor.execute(() -> {
            try {
                // 1. Vérifier localement
                if (evaluationDao.countByPfaId(pfaId) > 0) {
                    listener.onError("Ce projet a déjà été évalué.");
                    return;
                }

                // 2. Calculer le score total
                double totalScore = 0.0;
                double totalWeight = 0.0;
                for (CriteriaWithScore cs : criteriaScores) {
                    totalScore += cs.score * cs.getWeight();
                    totalWeight += cs.getWeight();
                }
                if (totalWeight > 0) {
                    totalScore = totalScore / totalWeight;
                }

                // 3. Insérer localement
                Evaluation evaluation = new Evaluation();
                evaluation.setPfa_id(pfaId);
                evaluation.setEvaluator_id(evaluatorId);
                evaluation.setDate_evaluation(System.currentTimeMillis());
                evaluation.setTotal_score(totalScore);
                long evalId = evaluationDao.insert(evaluation);

                // 4. Insérer les détails localement
                List<EvaluationDetail> details = new ArrayList<>();
                for (CriteriaWithScore cs : criteriaScores) {
                    EvaluationDetail detail = new EvaluationDetail();
                    detail.setEvaluation_id(evalId);
                    detail.setCriteria_id(cs.getCriteriaId());
                    detail.setScore_given(cs.score);
                    details.add(detail);
                }
                detailDao.insertAll(details);

                // 5. Mettre à jour le statut local
                Soutenance soutenance = soutenanceDao.getByPfaIdSync(pfaId);
                if (soutenance != null) {
                    soutenance.setStatus(SoutenanceStatus.DONE);
                    soutenanceDao.update(soutenance);
                }

                PFADossier pfa = pfaDao.getById(pfaId);
                if (pfa != null) {
                    pfa.setCurrent_status(PFAStatus.CLOSED);
                    pfa.setUpdated_at(System.currentTimeMillis());
                    pfaDao.update(pfa);
                }

                Log.d(TAG, "✅ Évaluation sauvegardée localement");

                // 6. Sync avec l'API
                syncSaveEvaluationWithApi(evaluatorId, pfaId, criteriaScores, totalScore, listener);

            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    private void syncSaveEvaluationWithApi(Long supervisorId, Long pfaId,
                                           List<CriteriaWithScore> criteriaScores,
                                           double totalScore,
                                           OnEvaluationListener listener) {
        EvaluationRequest request = new EvaluationRequest(pfaId, criteriaScores);

        apiService.saveEvaluation(supervisorId, request).enqueue(new Callback<ApiResponse<EvaluationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<EvaluationResponse>> call,
                                   Response<ApiResponse<EvaluationResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "✅ Évaluation sync avec API");
                }
                listener.onSuccess("Évaluation enregistrée avec succès ! Note: " +
                        String.format("%.1f", totalScore) + "/20");
            }

            @Override
            public void onFailure(Call<ApiResponse<EvaluationResponse>> call, Throwable t) {
                Log.w(TAG, "⚠️ API sync failed: " + t.getMessage());
                listener.onSuccess("Évaluation enregistrée (mode hors-ligne). Note: " +
                        String.format("%.1f", totalScore) + "/20");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTHODES EXISTANTES
    // ═══════════════════════════════════════════════════════════════════════

    public LiveData<Integer> getEvaluatedCount(Long supervisorId) {
        return evaluationDao.countBySupervisor(supervisorId);
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public interface OnEvaluationListener {
        void onSuccess(String message);
        void onError(String message);
    }


    public interface EvaluationListCallback {
        void onEvaluationsLoaded(List<Evaluation> evaluations);
    }


    public void getByPfaId(Long pfaId, EvaluationListCallback callback) {
        executor.execute(() -> {
            List<Evaluation> evaluations = evaluationDao.getByPfaId(pfaId);

            if (callback != null) {
                callback.onEvaluationsLoaded(evaluations);
            }
        });
    }

    public void forceRefresh(Long supervisorId) {
        syncEvaluationsFromApi(supervisorId);
        syncCriteriaFromApi();
    }
}