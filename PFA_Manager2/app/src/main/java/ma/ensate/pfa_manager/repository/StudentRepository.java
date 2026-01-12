package ma.ensate.pfa_manager.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.UserDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.EvaluationDao;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.Evaluation;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.StudentWithPFAResponse;
import ma.ensate.pfa_manager.model.dto.StudentWithPFA;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentRepository {

    private static final String TAG = "StudentRepository";

    private final UserDao userDao;
    private final PFADossierDao pfaDao;
    private final EvaluationDao evaluationDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncError = new MutableLiveData<>();

    public StudentRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        pfaDao = db.pfaDossierDao();
        evaluationDao = db.evaluationDao();
        apiService = ApiClient.getApiService();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * MÉTHODE PRINCIPALE : Retourne LiveData<Room> + Sync depuis API
     * ═══════════════════════════════════════════════════════════════════════
     *
     * 1. Retourne immédiatement le LiveData de Room (cache local)
     * 2. Lance un appel API en arrière-plan
     * 3. Quand l'API répond, sauvegarde dans Room
     * 4. Room notifie automatiquement le LiveData → UI se met à jour
     */
    public LiveData<List<StudentWithPFA>> getStudentsWithPFABySupervisor(Long supervisorId) {
        // 1. Synchroniser depuis l'API (en background)
        refreshFromApi(supervisorId);

        // 2. Retourner le LiveData de Room (source unique de vérité)
        return userDao.getStudentsWithPFABySupervisor(supervisorId);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * SYNCHRONISATION DEPUIS L'API
     * ═══════════════════════════════════════════════════════════════════════
     */
    public void refreshFromApi(Long supervisorId) {
        isSyncing.postValue(true);
        syncError.postValue(null);

        apiService.getMyStudents(supervisorId).enqueue(new Callback<ApiResponse<List<StudentWithPFAResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StudentWithPFAResponse>>> call,
                                   Response<ApiResponse<List<StudentWithPFAResponse>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<StudentWithPFAResponse>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        saveToLocalDatabase(apiResponse.getData(), supervisorId);
                        Log.d(TAG, "Sync réussie: " + apiResponse.getData().size() + " étudiants");
                    } else {
                        syncError.postValue(apiResponse.getMessage());
                        Log.e(TAG, "API error: " + apiResponse.getMessage());
                    }
                } else {
                    syncError.postValue("Erreur serveur: " + response.code());
                    Log.e(TAG, "HTTP error: " + response.code());
                }
                isSyncing.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<StudentWithPFAResponse>>> call, Throwable t) {
                syncError.postValue("Erreur réseau: " + t.getMessage());
                isSyncing.postValue(false);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * SAUVEGARDE DANS ROOM (Mapping API → Entités Room)
     * ═══════════════════════════════════════════════════════════════════════
     */
    // repository/StudentRepository.java - Méthode corrigée

    private void saveToLocalDatabase(List<StudentWithPFAResponse> studentsFromApi, Long supervisorId) {
        executor.execute(() -> {
            for (StudentWithPFAResponse apiStudent : studentsFromApi) {
                try {
                    // ═══════════════════════════════════════════════════════════
                    // 1. Sauvegarder/Mettre à jour l'User (étudiant)
                    // ═══════════════════════════════════════════════════════════
                    User existingStudent = userDao.getUserByIdSync(apiStudent.getStudentId());
                    boolean isNewStudent = (existingStudent == null);  // ← CORRECTION

                    User student;
                    if (isNewStudent) {
                        student = new User();
                    } else {
                        student = existingStudent;
                    }

                    // Mettre à jour les infos
                    student.setUser_id(apiStudent.getStudentId());
                    student.setFirst_name(apiStudent.getFirstName());
                    student.setLast_name(apiStudent.getLastName());
                    student.setEmail(apiStudent.getEmail());
                    student.setPhone_number(apiStudent.getPhoneNumber());
                    student.setRole(Role.STUDENT);
                    student.setCreated_at(System.currentTimeMillis());

                    if (isNewStudent) {
                        long insertedId = userDao.insert(student);
                        Log.d(TAG, "✅ INSERT User ID: " + insertedId);
                    } else {
                        userDao.update(student);
                        Log.d(TAG, "✅ UPDATE User ID: " + student.getUser_id());
                    }

                    // ═══════════════════════════════════════════════════════════
                    // 2. Sauvegarder/Mettre à jour le PFA
                    // ═══════════════════════════════════════════════════════════
                    if (apiStudent.getPfaId() != null) {
                        PFADossier existingPfa = pfaDao.getById(apiStudent.getPfaId());
                        boolean isNewPfa = (existingPfa == null);

                        PFADossier pfa;
                        if (isNewPfa) {
                            pfa = new PFADossier();
                        } else {
                            pfa = existingPfa;
                        }

                        pfa.setPfa_id(apiStudent.getPfaId());
                        pfa.setStudent_id(apiStudent.getStudentId());
                        pfa.setSupervisor_id(supervisorId);
                        pfa.setTitle(apiStudent.getPfaTitle());
                        pfa.setDescription(apiStudent.getPfaDescription());
                        pfa.setCurrent_status(mapPFAStatus(apiStudent.getPfaStatus()));
                        pfa.setUpdated_at(System.currentTimeMillis());

                        if (isNewPfa) {
                            long insertedId = pfaDao.insert(pfa);
                            Log.d(TAG, "✅ INSERT PFA ID: " + insertedId);
                        } else {
                            pfaDao.update(pfa);
                            Log.d(TAG, "✅ UPDATE PFA ID: " + pfa.getPfa_id());
                        }

                        // ═══════════════════════════════════════════════════════
                        // 3. Sauvegarder l'évaluation si existe
                        // ═══════════════════════════════════════════════════════
                        if (apiStudent.isEvaluated() && apiStudent.getTotalScore() != null) {
                            Evaluation existingEval = evaluationDao.getByPfaIdSync(apiStudent.getPfaId());
                            boolean isNewEval = (existingEval == null);

                            Evaluation eval;
                            if (isNewEval) {
                                eval = new Evaluation();
                            } else {
                                eval = existingEval;
                            }

                            eval.setPfa_id(apiStudent.getPfaId());
                            eval.setEvaluator_id(supervisorId);
                            eval.setTotal_score(apiStudent.getTotalScore());

                            if (isNewEval) {
                                evaluationDao.insert(eval);
                                Log.d(TAG, "INSERT Evaluation");
                            } else {
                                evaluationDao.update(eval);
                                Log.d(TAG, "UPDATE Evaluation");
                            }
                        }
                    }

                    Log.d(TAG, "══════════════════════════════════════════");
                    Log.d(TAG, "Sync complète pour: " + apiStudent.getFirstName() + " " + apiStudent.getLastName());
                    Log.d(TAG, "══════════════════════════════════════════");

                } catch (Exception e) {
                    Log.e(TAG, "Erreur sauvegarde: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * Mapper le status String vers l'enum PFAStatus
     */
    private PFAStatus mapPFAStatus(String status) {
        if (status == null) return PFAStatus.CONVENTION_PENDING;

        try {
            return PFAStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return PFAStatus.CONVENTION_PENDING;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * GETTERS POUR L'UI (états de synchronisation)
     * ═══════════════════════════════════════════════════════════════════════
     */
    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public LiveData<String> getSyncError() {
        return syncError;
    }

    /**
     * Forcer un refresh manuel (pull-to-refresh)
     */
    public void forceRefresh(Long supervisorId) {
        refreshFromApi(supervisorId);
    }
}