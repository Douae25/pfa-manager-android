package ma.ensate.pfa_manager.repository.coordinateur_filiere;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.EvaluationDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoordinatorRepository {

    private final UserDao userDao;
    private final PFADossierDao pfaDossierDao;
    private final EvaluationDao evaluationDao;
    private final ExecutorService executor;

    public CoordinatorRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        pfaDossierDao = db.pfaDossierDao();
        evaluationDao = db.evaluationDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void getStudentsWithEvaluations(long departmentId, Callback<List<StudentWithEvaluation>> callback) {
        executor.execute(() -> {
            try {
                // 1. Récupérer tous les étudiants du département
                List<User> students = userDao.getUsersByDepartmentAndRole(departmentId, Role.STUDENT);
                List<StudentWithEvaluation> result = new ArrayList<>();

                for (User student : students) {
                    StudentWithEvaluation swe = new StudentWithEvaluation();
                    swe.setStudent(student);

                    // 2. Récupérer le PFA de l'étudiant (premier de la liste)
                    List<PFADossier> pfaList = pfaDossierDao.getByStudent(student.getUser_id());
                    if (pfaList != null && !pfaList.isEmpty()) {
                        PFADossier pfa = pfaList.get(0); // Prendre le premier PFA
                        swe.setPfaDossier(pfa);

                        // 3. Récupérer l'encadrant
                        if (pfa.getSupervisor_id() != null) {
                            User supervisor = userDao.getUserById(pfa.getSupervisor_id());
                            swe.setSupervisor(supervisor);
                        }

                        // 4. Récupérer l'évaluation
                        List<Evaluation> evaluations = evaluationDao.getByPfaId(pfa.getPfa_id());
                        if (evaluations != null && !evaluations.isEmpty()) {
                            // Prendre la dernière évaluation
                            swe.setEvaluation(evaluations.get(evaluations.size() - 1));
                        }
                    }

                    result.add(swe);
                }

                callback.onSuccess(result);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Dans la méthode getProfessorsWithStats()
    public void getProfessorsWithStats(long departmentId, Callback<List<ProfessorWithStats>> callback) {
        executor.execute(() -> {
            try {
                // 1. Récupérer tous les professeurs du département
                List<User> professors = userDao.getUsersByDepartmentAndRole(departmentId, Role.PROFESSOR);
                List<ProfessorWithStats> result = new ArrayList<>();

                for (User professor : professors) {
                    ProfessorWithStats pws = new ProfessorWithStats();
                    pws.setProfessor(professor);

                    // 2. Compter les étudiants assignés
                    List<PFADossier> dossiers = pfaDossierDao.getBySupervisor(professor.getUser_id());
                    pws.setStudentCount(dossiers != null ? dossiers.size() : 0);

                    // 3. Calculer la moyenne des scores (supprimer projectCount)
                    if (dossiers != null && !dossiers.isEmpty()) {
                        double totalScore = 0;
                        int evaluatedCount = 0;

                        for (PFADossier dossier : dossiers) {
                            List<Evaluation> evaluations = evaluationDao.getByPfaId(dossier.getPfa_id());
                            if (evaluations != null && !evaluations.isEmpty()) {
                                Evaluation lastEval = evaluations.get(evaluations.size() - 1);
                                if (lastEval.getTotal_score() != null) {
                                    totalScore += lastEval.getTotal_score();
                                    evaluatedCount++;
                                }
                            }
                        }

                        pws.setAverageScore(evaluatedCount > 0 ? totalScore / evaluatedCount : null);
                    }

                    result.add(pws);
                }

                callback.onSuccess(result);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}