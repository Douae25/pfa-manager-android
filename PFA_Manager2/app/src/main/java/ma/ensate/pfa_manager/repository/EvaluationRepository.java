package ma.ensate.pfa_manager.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

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
import ma.ensate.pfa_manager.model.Evaluation;
import ma.ensate.pfa_manager.model.EvaluationCriteria;
import ma.ensate.pfa_manager.model.EvaluationDetail;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.SoutenanceStatus;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.dto.CriteriaWithScore;
import ma.ensate.pfa_manager.model.dto.SoutenanceWithEvaluation;

public class EvaluationRepository {

    private final EvaluationDao evaluationDao;
    private final EvaluationCriteriaDao criteriaDao;
    private final EvaluationDetailDao detailDao;
    private final SoutenanceDao soutenanceDao;
    private final PFADossierDao pfaDao;
    private final UserDao userDao;
    private final ExecutorService executor;

    public EvaluationRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        evaluationDao = db.evaluationDao();
        criteriaDao = db.evaluationCriteriaDao();
        detailDao = db.evaluationDetailDao();
        soutenanceDao = db.soutenanceDao();
        pfaDao = db.pfaDossierDao();
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<EvaluationCriteria>> getActiveCriteria() {
        return criteriaDao.getActiveLive();
    }

    public LiveData<List<SoutenanceWithEvaluation>> getSoutenancesWithEvaluations(Long supervisorId) {
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


    public LiveData<Integer> getEvaluatedCount(Long supervisorId) {
        return evaluationDao.countBySupervisor(supervisorId);
    }

    public void saveEvaluation(Long pfaId, Long evaluatorId, List<CriteriaWithScore> criteriaScores,
                               OnEvaluationListener listener) {
        executor.execute(() -> {
            try {
                if (evaluationDao.countByPfaId(pfaId) > 0) {
                    listener.onError("Ce projet a déjà été évalué.");
                    return;
                }

                double totalScore = 0.0;
                double totalWeight = 0.0;
                for (CriteriaWithScore cs : criteriaScores) {
                    totalScore += cs.score * cs.getWeight();
                    totalWeight += cs.getWeight();
                }
                if (totalWeight > 0) {
                    totalScore = totalScore / totalWeight;
                }

                Evaluation evaluation = new Evaluation();
                evaluation.setPfa_id(pfaId);
                evaluation.setEvaluator_id(evaluatorId);
                evaluation.setDate_evaluation(System.currentTimeMillis());
                evaluation.setTotal_score(totalScore);
                long evalId = evaluationDao.insert(evaluation);

                List<EvaluationDetail> details = new ArrayList<>();
                for (CriteriaWithScore cs : criteriaScores) {
                    EvaluationDetail detail = new EvaluationDetail();
                    detail.setEvaluation_id(evalId);
                    detail.setCriteria_id(cs.getCriteriaId());
                    detail.setScore_given(cs.score);
                    details.add(detail);
                }
                detailDao.insertAll(details);

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

                listener.onSuccess("Évaluation enregistrée avec succès ! Note: " + String.format("%.1f", totalScore) + "/20");

            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    public void getByPfaId(Long pfaId, OnEvaluationsFetchedListener listener) {
        executor.execute(() -> {
            if (pfaId != null) {
                List<Evaluation> results = evaluationDao.getByPfaId(pfaId);
                if (listener != null) {
                    listener.onEvaluationsFetched(results);
                }
            }
        });
    }
    public interface OnEvaluationsFetchedListener {
        void onEvaluationsFetched(List<Evaluation> evaluations);
    }
    public interface OnEvaluationListener {
        void onSuccess(String message);
        void onError(String message);
    }
}