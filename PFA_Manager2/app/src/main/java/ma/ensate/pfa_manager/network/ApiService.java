package ma.ensate.pfa_manager.network;

import java.util.List;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.StudentWithPFAResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @GET("supervisor/students")
    Call<ApiResponse<List<StudentWithPFAResponse>>> getMyStudents(
            @Query("supervisorId") Long supervisorId
    );
}