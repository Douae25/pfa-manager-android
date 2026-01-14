package ma.ensate.pfa_manager.network;

import java.util.List;

import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.ConventionRequest;
import ma.ensate.pfa_manager.model.api.ConventionResponse;
import ma.ensate.pfa_manager.model.api.EvaluationCriteriaResponse;
import ma.ensate.pfa_manager.model.api.EvaluationRequest;
import ma.ensate.pfa_manager.model.api.EvaluationResponse;
import ma.ensate.pfa_manager.model.api.PFADossierRequest;
import ma.ensate.pfa_manager.model.api.PFADossierResponse;
import ma.ensate.pfa_manager.model.api.PFAWithSoutenanceResponse;
import ma.ensate.pfa_manager.model.api.SoutenanceRequest;
import ma.ensate.pfa_manager.model.api.SoutenanceResponse;
import ma.ensate.pfa_manager.model.api.SoutenanceWithEvaluationResponse;
import ma.ensate.pfa_manager.model.api.StudentWithPFAResponse;
import ma.ensate.pfa_manager.model.api.StudentDetailResponse;
import ma.ensate.pfa_manager.model.api.DeliverableRequest;
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
    // CONVENTIONS
    // ════════════════════════════════════════════════════════════

    // Student: Request internship convention (POST demande de convention)
    @POST("conventions")
    Call<ConventionResponse> requestConvention(
            @Body ConventionRequest request
    );

    // Student: Upload signed convention (POST fichier signé)
    @POST("conventions/{id}/upload-signed")
    Call<ConventionResponse> uploadSignedConvention(
            @Path("id") Long conventionId,
            @Query("scannedFileUri") String scannedFileUri
    );

    // Student: Get convention by ID (GET consulter convention)
    @GET("conventions/{id}")
    Call<ConventionResponse> getConventionById(
            @Path("id") Long conventionId
    );

    // ════════════════════════════════════════════════════════════
    // DELIVERABLES
    // ════════════════════════════════════════════════════════════

    // Student: Deposit deliverable (POST déposer livrable)
    @POST("deliverables")
    Call<DeliverableResponse> depositDeliverable(
            @Body DeliverableRequest request
    );

    // Student: Get deliverable by ID (GET consulter livrable)
    @GET("deliverables/{id}")
    Call<DeliverableResponse> getDeliverableById(
            @Path("id") Long deliverableId
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

    // Student: Consult defense date by PFA ID
    @GET("soutenances/pfa/{pfaId}")
    Call<ApiResponse<SoutenanceResponse>> getSoutenanceByPfaId(
            @Path("pfaId") Long pfaId
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

    // Student: View evaluation by ID
    @GET("evaluations/{id}")
    Call<ApiResponse<EvaluationResponse>> getEvaluationById(
            @Path("id") Long evaluationId
    );

    // ════════════════════════════════════════════════════════════
    // PFA DOSSIER (Student)
    // ════════════════════════════════════════════════════════════

    @GET("pfa-dossiers/student/{studentId}")
    Call<List<PFADossierResponse>> getPFADossiersByStudent(
            @Path("studentId") Long studentId
    );

    @POST("pfa-dossiers/create-or-get")
    Call<PFADossierResponse> createOrGetPFADossier(
            @Body PFADossierRequest request
    );

    // ════════════════════════════════════════════════════════════
    // CONVENTIONS (Student)
    // ════════════════════════════════════════════════════════════

    @GET("conventions/pfa/{pfaId}")
    Call<ConventionResponse> getConventionByPfaId(
            @Path("pfaId") Long pfaId
    );

    // ════════════════════════════════════════════════════════════
    // DELIVERABLES (Student)
    // ════════════════════════════════════════════════════════════

    @GET("deliverables/pfa/{pfaId}")
    Call<List<DeliverableResponse>> getDeliverablesByPfaId(
            @Path("pfaId") Long pfaId
    );

    // ════════════════════════════════════════════════════════════
    // EVALUATIONS (Student)
    // ════════════════════════════════════════════════════════════

    @GET("evaluations/pfa/{pfaId}")
    Call<List<EvaluationResponse>> getEvaluationsByPfaId(
            @Path("pfaId") Long pfaId
    );

}