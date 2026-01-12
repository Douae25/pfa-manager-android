package ma.ensate.pfa_manager.network;

import java.util.List;

import ma.ensate.pfa_manager.model.api.ApiResponse;
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
}