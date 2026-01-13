package ma.ensate.pfa_manager.network;

import java.util.List;

import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.EvaluationCriteriaResponse;
import ma.ensate.pfa_manager.model.api.EvaluationRequest;
import ma.ensate.pfa_manager.model.api.EvaluationResponse;
import ma.ensate.pfa_manager.model.api.PFAWithSoutenanceResponse;
import ma.ensate.pfa_manager.model.api.SoutenanceRequest;
import ma.ensate.pfa_manager.model.api.SoutenanceResponse;
import ma.ensate.pfa_manager.model.api.SoutenanceWithEvaluationResponse;
import ma.ensate.pfa_manager.model.api.StudentWithPFAResponse;
import ma.ensate.pfa_manager.model.api.StudentDetailResponse;
import ma.ensate.pfa_manager.model.api.DeliverableResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ════════════════════════════════════════════════════════════
    // STUDENTS
    // ════════════════════════════════════════════════════════════

    @GET("supervisor/students")
    Call<ApiResponse<List<StudentWithPFAResponse>>> getMyStudents(
            @Query("supervisorId") Long supervisorId
    );

    @GET("supervisor/students/{studentId}")
    Call<ApiResponse<StudentDetailResponse>> getStudentDetail(
            @Path("studentId") Long studentId,
            @Query("supervisorId") Long supervisorId

    );

    // ════════════════════════════════════════════════════════════
    // DELIVERABLES
    // ════════════════════════════════════════════════════════════

    @GET("supervisor/deliverables")
    Call<ApiResponse<List<DeliverableResponse>>> getAllDeliverables(
            @Query("supervisorId") Long supervisorId
    );

    @GET("supervisor/deliverables/pfa/{pfaId}")
    Call<ApiResponse<List<DeliverableResponse>>> getDeliverablesByPfa(
            @Path("pfaId") Long pfaId
    );

    // ════════════════════════════════════════════════════════════
    // SOUTENANCES
    // ════════════════════════════════════════════════════════════
    @GET("supervisor/soutenances/pfas")
    Call<ApiResponse<List<PFAWithSoutenanceResponse>>> getPFAsWithSoutenances(
            @Query("supervisorId") Long supervisorId
    );

    @GET("supervisor/soutenances")
    Call<ApiResponse<List<SoutenanceResponse>>> getSoutenances(
            @Query("supervisorId") Long supervisorId
    );

    @POST("supervisor/soutenances")
    Call<ApiResponse<SoutenanceResponse>> planifierSoutenance(
            @Query("supervisorId") Long supervisorId,
            @Body SoutenanceRequest request
    );

    @PUT("supervisor/soutenances/{soutenanceId}")
    Call<ApiResponse<SoutenanceResponse>> modifierSoutenance(
            @Path("soutenanceId") Long soutenanceId,
            @Query("supervisorId") Long supervisorId,
            @Body SoutenanceRequest request
    );

    @DELETE("supervisor/soutenances/{soutenanceId}")
    Call<ApiResponse<Void>> supprimerSoutenance(
            @Path("soutenanceId") Long soutenanceId,
            @Query("supervisorId") Long supervisorId

    );

    // ════════════════════════════════════════════════════════════
    // EVALUATIONS
    // ════════════════════════════════════════════════════════════

    @GET("supervisor/evaluation-criteria")
    Call<ApiResponse<List<EvaluationCriteriaResponse>>> getEvaluationCriteria();

    @GET("supervisor/evaluations")
    Call<ApiResponse<List<SoutenanceWithEvaluationResponse>>> getSoutenancesWithEvaluations(
            @Query("supervisorId") Long supervisorId
    );

    @POST("supervisor/evaluations")
    Call<ApiResponse<EvaluationResponse>> saveEvaluation(
            @Query("supervisorId") Long supervisorId,
            @Body EvaluationRequest request
    );

}